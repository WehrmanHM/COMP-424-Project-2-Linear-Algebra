import scalafx.application.JFXApp3
import scalafx.scene.Scene
import scalafx.scene.layout.{BorderPane, HBox, VBox}
import scalafx.scene.control.*
import scalafx.scene.canvas.Canvas
import scalafx.scene.paint.Color
import scalafx.animation.AnimationTimer
import scalafx.scene.text.{Font, FontWeight}

import scala.concurrent.*
import scala.concurrent.duration.*
import ExecutionContext.Implicits.global

object PlotDemo extends JFXApp3 {
  type Pt4D = (Double, Double, Double, Double)
  val cores: Int = Runtime.getRuntime.availableProcessors()

  def transformSequential(pts: Array[Pt4D], M: Matrix): Array[Pt4D] = {
    pts.map { case (x, y, z, w) =>
      val v = M * Array(x, y, z, w)
      (v(0), v(1), v(2), v(3))
    }
  }

  def transformParallel(pts: Array[Pt4D], M: Matrix): Array[Pt4D] = {
    val groups = pts.grouped((pts.length+cores-1)/cores).toSeq
    val futures = groups.map(chunk => Future {
      chunk.map { case (x, y, z, w) =>
        val v = M * Array(x, y, z, w)
        (v(0), v(1), v(2), v(3))
      }
    })
    Await.result(Future.sequence(futures), Duration.Inf).flatten.toArray
  }

  def transformGPU(pts: Array[Pt4D], M: Matrix): Array[Pt4D] = {
    val ptsIn = new Array[Float](pts.length*4)
    pts.zipWithIndex.foreach { case ((x, y, z, w), i) =>
      val idx = i*4
      ptsIn(idx) = x.toFloat ; ptsIn(idx+1) = y.toFloat ; ptsIn(idx+2) = z.toFloat; ptsIn(idx+3) = w.toFloat
    }
    val MArray = M.contents.flatten.map(_.toFloat)
    val floatsOut = GpuTransformer.transform(ptsIn, MArray)
    floatsOut.grouped(4).map { case Array(x, y, z, w) =>
      (x.toDouble, y.toDouble, z.toDouble, w.toDouble)
    }.toArray
  }

  def makePoints(n: Int): Array[Pt4D] = {
    Array.fill(n) {
      val x = math.random*200-100
      val y = math.random*200-100
      val z = math.random*200-100
      (x, y, z, 1.0)
    }
  }

  def generateSphere(n: Int, radius: Double = 1.0): Array[Pt4D] = {
    Array.fill(n) {
      // u and v are random variables that allow us to cover the sphere
      val u = rnd.nextDouble()
      val v = rnd.nextDouble()
      val theta = 2 * Math.PI * u
      val phi = math.acos(2*v-1)
      val x = radius * math.sin(phi) * math.cos(theta)
      val y = radius * math.sin(phi) * math.sin(theta)
      val z = radius * math.cos(phi)
      (x, y, z, 1.0)
    }
  }

  def generateDonut(n: Int, a: Double = 1.0, c: Double = 2.0): Array[Pt4D] = {
    Array.fill(n) {
      val u = rnd.nextDouble()*2*Math.PI
      val v = rnd.nextDouble()*2*Math.PI
      var g = a * math.abs(c+math.cos(v)*a)
      while (g < rnd.nextDouble()*(a*(a+c))) do {
        val u = rnd.nextDouble() * 2 * Math.PI
        val v = rnd.nextDouble() * 2 * Math.PI
        g = a * math.abs(c+math.cos(v)*a)
      }
      val x = (c + a * math.cos(v)) * math.cos(u)
      val y = (c + a * math.cos(v)) * math.sin(u)
      val z = a * math.sin(v)
      (x, y, z, 1.0)
    }
  }

  override def start(): Unit = {
    val slider = new Slider(1000, 200000, 50000) {
      showTickLabels = true
      showTickMarks = true
      majorTickUnit = 50000
    }

    val cpuButton = new RadioButton("CPU") {selected = true}
    val parallelButton = new RadioButton("PAR")
    val gpuButton = new RadioButton("GPU")
    val toggleGrp = new ToggleGroup()
    cpuButton.toggleGroup = toggleGrp
    parallelButton.toggleGroup = toggleGrp
    gpuButton.toggleGroup = toggleGrp
    val timeLabel = new Label("0.0 ms") {font = Font.font("Arial", FontWeight.Bold, 20); textFill = Color.Yellow}

    val toolbar = new HBox(10, new Label("Mode: "), cpuButton, parallelButton, gpuButton)

    // Canvas
    val sceneW = 800; val sceneH = 600
    val fov = math.toRadians(60)
    val f = (sceneH/2) / math.tan(fov/2)
    val cx = sceneW/2
    val cy = sceneH/2
    val canvas = new Canvas(sceneW, sceneH)
    val gc = canvas.graphicsContext2D

    // Compute matrix

    val rotationMatrix3 = generateRotationMatrix(15, 'z')
    val rotationMatrix2 = generateRotationMatrix(3, 'y')
    val translationMatrix = generateTranslationMatrix(Array(0, 0, 0.01))
    val povMatrix = generateViewMatrix(Array(0, 0, 0))
    val compositeMatrix = rotationMatrix3 * rotationMatrix2 * translationMatrix * povMatrix

    val minInterval: Long = 100000000L // 100 ms = 100,000,000 ns
    var lastUpdate: Long = 0L
    var oldCount: Int = 0
    var pts: Array[Pt4D] = new Array(50000)
    val timer = AnimationTimer { now =>
      if (now - lastUpdate >= minInterval) {
        //lastUpdate = now
        val n = slider.value.value.toInt

        if (n != oldCount) then {
          oldCount = n
          //pts = generateSphere(n, 4)
          pts = generateDonut(n, 0.5, 1.0)
          pts.map((x, y, z, w) => (x, y, z+50, w))
        }
        val M = compositeMatrix

        val t0 = System.nanoTime()
        pts =
          if (cpuButton.selected.value) transformSequential(pts, M)
          else if (parallelButton.selected.value) transformParallel(pts, M)
          else transformGPU(pts, M)

        val dt = (System.nanoTime() - t0) / 1e6
        timeLabel.text = f"${dt % .1f} ms"

        // DRAW
        gc.fill = Color.Black;
        gc.fillRect(0, 0, sceneW, sceneH)
        gc.fill = Color.White
        pts.foreach { case (x, y, z, _) =>
//          val pX = (x / z) * (sceneW / 2) + (sceneW / 2)
//          val pY = (y / z) * (sceneH / 2) + (sceneH / 2)
          val pX = (f*x) / z
          val pY = (f*y) / z
          val screenX = pX + cx
          val screenY = -pY + cy
          gc.fillOval(screenX, screenY, 2, 2)
        }
      }
    }
    timer.start()

    stage = new JFXApp3.PrimaryStage {
      title = "Transform Demo"
      scene = new Scene (
       new BorderPane {
          top = new VBox(toolbar, slider)
          center = canvas
        }
      )
    }
  }
}
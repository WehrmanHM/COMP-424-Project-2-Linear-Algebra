import org.scalatest.funsuite.AnyFunSuite

class demoSpec extends AnyFunSuite {

  test("transformSequential produces correct results") {
    val points = Array((1.0, 2.0, 3.0, 1.0), (4.0, 5.0, 6.0, 1.0))
    val matrix = new Matrix(Array(
      Array(1.0, 0.0, 0.0, 0.0),
      Array(0.0, 1.0, 0.0, 0.0),
      Array(0.0, 0.0, 1.0, 0.0),
      Array(0.0, 0.0, 0.0, 1.0)
    ))

    val result = PlotDemo.transformSequential(points, matrix)

    assert(result.sameElements(points))
  }

  test("transformParallel produces the same results as transformSequential") {
    val points = Array((1.0, 2.0, 3.0, 1.0), (4.0, 5.0, 6.0, 1.0))
    val matrix = new Matrix(Array(
      Array(1.0, 0.0, 0.0, 0.0),
      Array(0.0, 1.0, 0.0, 0.0),
      Array(0.0, 0.0, 1.0, 0.0),
      Array(0.0, 0.0, 0.0, 1.0)
    ))

    val sequentialResult = PlotDemo.transformSequential(points, matrix)
    val parallelResult = PlotDemo.transformParallel(points, matrix)

    assert(parallelResult.sameElements(sequentialResult))
  }

  test("transformGPU produces the same results as transformSequential") {
    val points = Array((1.0, 2.0, 3.0, 1.0), (4.0, 5.0, 6.0, 1.0))
    val matrix = new Matrix(Array(
      Array(1.0, 0.0, 0.0, 0.0),
      Array(0.0, 1.0, 0.0, 0.0),
      Array(0.0, 0.0, 1.0, 0.0),
      Array(0.0, 0.0, 0.0, 1.0)
    ))

    val sequentialResult = PlotDemo.transformSequential(points, matrix)
    val gpuResult = PlotDemo.transformGPU(points, matrix)

    assert(gpuResult.sameElements(sequentialResult))
  }

  test("makePoints generates points within the expected range") {
    val points = PlotDemo.makePoints(1000)

    assert(points.length == 1000)
    assert(points.forall { case (x, y, z, w) =>
      x >= -100 && x <= 100 &&
      y >= -100 && y <= 100 &&
      z >= -100 && z <= 100 &&
      w == 1.0
    })
  }

  test("generateSphere generates points on a sphere of the given radius") {
    val radius = 5.0
    val points = PlotDemo.generateSphere(1000, radius)

    assert(points.length == 1000)
    assert(points.forall { case (x, y, z, w) =>
      math.abs(math.sqrt(x * x + y * y + z * z) - radius) < 1e-6 &&
      w == 1.0
    })
  }
}
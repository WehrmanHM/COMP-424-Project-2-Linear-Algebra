import java.util.concurrent.ForkJoinPool
import scala.collection.parallel.CollectionConverters.*
import scala.concurrent.duration.*
import scala.concurrent.*
import scala.math.{cos, sin}
import scala.util.Random

val rnd = new Random()

@main
def main(): Unit = {
  println("Hello World")
  println("")

  val sizes = List(10, 100, 10000, 1000000, 10000000)
  val rotationMatrix = generateRotationMatrix(45, 'x')
  val transformationMatrix = generateTranslationMatrix(Array(3, 5, -3))
  val viewMatrix = generateViewMatrix(Array(6, -3, -2))

  val pointCloud = generatePointCloud(500000, 100, 100, 100)


//  for (size <- sizes) {
//    println(s"Testing with point cloud size: $size")
//    val pointCloud = generatePointCloud(size, 50, 50, 50)
//
//    // Sequential matrix multiplication
//    withTimeout(30.seconds) {
//      val sequentialStart = System.nanoTime()
//      val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix
//      val sequentialResult = pointCloud.map(point => compositeMatrix * point)
//      val sequentialEnd = System.nanoTime()
//      val sequentialTime = (sequentialEnd - sequentialStart) / 1e6
//      println(f"Sequential execution time: $sequentialTime%.2f ms")
//    }
//
//    // Advanced parallel matrix multiplication
//    withTimeout(30.seconds) {
//      val advancedParallelStart = System.nanoTime()
//      val compositeMatrix = rotationMatrix.parallelMultiply(transformationMatrix).parallelMultiply(viewMatrix)
//      val advancedParallelResult = pointCloud.par.map(point => compositeMatrix * point)
//      val advancedParallelEnd = System.nanoTime()
//      val advancedParallelTime = (advancedParallelEnd - advancedParallelStart) / 1e6
//      println(f"Advanced parallel execution time: $advancedParallelTime%.2f ms")
//    }
//
//    // Optimized parallel matrix multiplication
//    withTimeout(30.seconds) {
//      val optimizedParallelStart = System.nanoTime()
//      val compositeMatrix = rotationMatrix.optimizedParallelMultiply(transformationMatrix).optimizedParallelMultiply(viewMatrix)
//      val optimizedParallelResult = pointCloud.par.map(point => compositeMatrix * point)
//      val optimizedParallelEnd = System.nanoTime()
//      val optimizedParallelTime = (optimizedParallelEnd - optimizedParallelStart) / 1e6
//      println(f"Optimized parallel execution time: $optimizedParallelTime%.2f ms")
//    }
//
//    // Futures implementation of the transformation process on all CPU cores
//    // not significantly different from the .par implementations
//    withTimeout(30.seconds) {
//      val optimizedParallelStart = System.nanoTime()
//      val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix
//      val optimizedParallelResult = pointCloudParallel(pointCloud.toArray, compositeMatrix)
//      val optimizedParallelEnd = System.nanoTime()
//      val optimizedParallelTime = (optimizedParallelEnd - optimizedParallelStart) / 1e6
//      println(f"Optimized parallel execution time: $optimizedParallelTime%.2f ms")
//    }
//
    // Don't uncomment this
    gpuParallelism()
    
    println("")
  System.exit(0)
}

/**
 * generates {size} points in 3D space and returns them
 * @param size how many points to generate
 * @param xBound the boundaries of the x-axis
 * @param yBound the boundaries of the y-axis
 * @param zBound the boundaries of the z-axis
 * @return an iterable of 3D homogeneous vectors
 */
def generatePointCloud(size: Int, xBound: Int, yBound: Int, zBound: Int): Iterable[Array[Double]] = {
  def generatePoint(): Array[Double] = {
    val x = rnd.nextInt(xBound) * (if isNegative then -1 else 1)
    val y = rnd.nextInt(yBound) * (if isNegative then -1 else 1)
    val z = rnd.nextInt(zBound) * (if isNegative then -1 else 1)
    Array(x, y, z, 1.0)
  }
  for i <- 0 to size yield generatePoint()
}

/**
 * generates a 4x4 rotation matrix for a given angle and axis
 * @param angle the angle of rotation, in degrees
 * @param axis the axis being rotated around
 * @return the rotation matrix
 */
def generateRotationMatrix(angle: Double, axis: Char): Matrix = {
  val angleRad = angle.toRadians
  axis match
    case 'x' =>
      val row1 = Array(1.0, 0, 0, 0)
      val row2 = Array(0, cos(angleRad), -sin(angleRad), 0)
      val row3 = Array(0, sin(angleRad), cos(angleRad), 0)
      val row4 = Array(0, 0, 0, 1.0)
      new Matrix(Array(row1, row2, row3, row4))
    case 'y' =>
      val row1 = Array(cos(angleRad), 0, sin(angleRad), 0)
      val row2 = Array(0, 1.0, 0, 0)
      val row3 = Array(-sin(angleRad), 0, cos(angleRad), 0)
      val row4 = Array(0, 0, 0, 1.0)
      new Matrix(Array(row1, row2, row3, row4))
    case 'z' =>
      val row1 = Array(cos(angleRad), -sin(angleRad), 0, 0)
      val row2 = Array(sin(angleRad), cos(angleRad), 0, 0)
      val row3 = Array(0, 0, 1.0, 0)
      val row4 = Array(0, 0, 0, 1.0)
      new Matrix(Array(row1, row2, row3, row4))
}

/**
 * generates a 4x4 translation matrix for a given translation vector
 * @param translation the desired translation as a vector (x, y, z)
 * @return the translation matrix
 */
def generateTranslationMatrix(translation: Array[Double]): Matrix = {
  val x = translation(0)
  val y = translation(1)
  val z = translation(2)

  new Matrix(Array(Array(1, 0, 0, x), Array(0, 1, 0, y), Array(0, 0, 1, z), Array(0, 0, 0, 1.0)))
}


/**
 * generates a 4x4 translation matrix to simulate camera movement
 * it's just a translation matrix with the direction inverted,
 * i.e. to move the camera forward 5 units, we move everything backwards 5 units (closer to the camera)
 * @param cameraMovement the desired camera movement as a vector (x, y, z)
 * @return the camera view matrix
 */
def generateViewMatrix(cameraMovement: Array[Double]): Matrix = {
  generateTranslationMatrix(cameraMovement.map(_ * -1))
}

/**
 * generate a 4x4 matrix that simulates mapping 3D space in two dimensions, providing perspective
 * @param left the left bound of the space (-x)
 * @param right the right bound of the space (+x)
 * @param top the upper bound of the space (+y)
 * @param bottom the lower bound of the space (-y)
 * @param near the close bound of the space (-z)
 * @param far the far bound of the space (+z)
 * @return the projection matrix
 */
def generateProjectionMatrix(left: Double, right: Double, top: Double, bottom: Double, near: Double, far: Double): Matrix = {
  val row1 = Array(2/(right-left), 0, 0, -(right+left)/(right-left))
  val row2 = Array(0, 2/(top-bottom), 0, -(top+bottom)/(top-bottom))
  val row3 = Array(0, 0, -2*(far-near), -(far+near)/(far-near))
  val row4 = Array(0, 0, 0.0, 0.0)
  new Matrix(Array(row1, row2, row3, row4))
}

def gpuParallelism(): Unit = {
  val rotationMatrix = generateRotationMatrix(53, 'y')
  val transformationMatrix = generateTranslationMatrix(Array(1.2, 7.0, 9.3))
  val viewMatrix = generateViewMatrix(Array(1, -6, 4))
  val pointCloud = generatePointCloud(100000000, 100, 100, 100)
  var total = 0.0
  val runs = 50
  for i <- 1 to runs do {
    val gpuStart = System.nanoTime()
    val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix

    // flatten the 4x4 matrix in a 16-element Float array
    val M: Array[Float] = compositeMatrix.contents.flatten.map(_.toFloat)
    // do the same thing to the entire point cloud (easier to work with in memory)
    val inFlat: Array[Float] = pointCloud.flatMap(v => Array(v(0).toFloat, v(1).toFloat, v(2).toFloat, 1.0f)).toArray
    // go go gadget, GPU (outFlat is what we get back)
    val outFlat: Array[Float] = GpuTransformer.transform(inFlat, M)
    // give the flattened array some grow juice and turn it back into a Matrix
    val transformed: Matrix = new Matrix(outFlat.grouped(4).map { case Array(x, y, z, w) =>
      Array(x.toDouble, y.toDouble, z.toDouble, w.toDouble)
    }.toArray)
    val gpuStop = System.nanoTime()
    val gpuTime = (gpuStop - gpuStart) / 1e6
    if (i != 1) then total = total + gpuTime
    println(s"Execution time: $gpuTime ms")
    println(s"Transformed ${pointCloud.size - 1} points on GPU.")
  }
  println(s"Average execution time of ${total/runs-1} ms")
}

def pointCloudParallel(pointCloud: Array[Array[Double]], composite: Matrix, parallelism: Int = Runtime.getRuntime.availableProcessors()): Array[Array[Double]] = {
  val fjp = new ForkJoinPool(parallelism)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(fjp)

  val chunkSize = (pointCloud.length +parallelism-1)/parallelism
  val chunks: Seq[Array[Array[Double]]] = pointCloud.grouped(chunkSize).map(_.toArray).toSeq

  val futures: Seq[Future[Array[Array[Double]]]] = chunks.map {
    chunk => Future {
      chunk.map(composite * _)
    }
  }

  val result = Await.result(Future.sequence(futures), Duration.Inf).flatten.toArray
  fjp.shutdown()
  result
}

/**
 * returns a random true/false value
 * @return true or false
 */
def isNegative: Boolean = {
  rnd.nextBoolean()
}

/**
 * Executes a block of code with a timeout. If the block takes longer than the specified duration,
 * it will throw a TimeoutException.
 * @param duration the maximum allowed duration
 * @param block the block of code to execute
 * @tparam T the return type of the block
 * @return the result of the block if it completes within the duration
 */
def withTimeout[T](duration: Duration)(block: => T): Option[T] = {
  try {
    Some(Await.result(Future(block)(ExecutionContext.global), duration))
  } catch {
    case _: TimeoutException =>
      println("This method takes too long")
      None
  }
}

/**
 * Compares two vectors of doubles with a tolerance to account for floating-point precision errors.
 * @param v1 the first vector
 * @param v2 the second vector
 * @param tolerance the allowed difference between corresponding elements
 * @return true if the vectors are approximately equal, false otherwise
 */
def areVectorsEqual(v1: Vector[Double], v2: Vector[Double], tolerance: Double = 1e-9): Boolean = {
  v1.zip(v2).forall { case (a, b) => Math.abs(a - b) <= tolerance }
}

/**
 * Compares two collections of vectors with a tolerance.
 * @param c1 the first collection
 * @param c2 the second collection
 * @param tolerance the allowed difference between corresponding elements
 * @return true if the collections are approximately equal, false otherwise
 */
def areCollectionsEqual(c1: Iterable[Vector[Double]], c2: Iterable[Vector[Double]], tolerance: Double = 1e-9): Boolean = {
  c1.size == c2.size && c1.zip(c2).forall { case (v1, v2) => areVectorsEqual(v1, v2, tolerance) }
}

/**
 * Rounds small floating-point values to 0.0 within a specified tolerance.
 * @param value the floating-point value
 * @param tolerance the tolerance for rounding
 * @return the rounded value
 */
def roundToZero(value: Double, tolerance: Double = 1e-9): Double = {
  if (Math.abs(value) <= tolerance) 0.0 else value
}

/**
 * Applies rounding to all elements in a vector.
 * @param vector the vector of doubles
 * @param tolerance the tolerance for rounding
 * @return the rounded vector
 */
def roundVector(vector: Vector[Double], tolerance: Double = 1e-9): Vector[Double] = {
  vector.map(roundToZero(_, tolerance))
}

/**
 * Applies rounding to all vectors in a collection.
 * @param collection the collection of vectors
 * @param tolerance the tolerance for rounding
 * @return the rounded collection
 */
def roundCollection(collection: Iterable[Vector[Double]], tolerance: Double = 1e-9): Iterable[Vector[Double]] = {
  collection.map(roundVector(_, tolerance))
}
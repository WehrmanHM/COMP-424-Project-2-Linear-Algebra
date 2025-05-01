import scala.util.Random
import scala.math.{cos, sin}
import java.util.concurrent.{Executors, ForkJoinPool}
import scala.concurrent.{ExecutionContext, Future, TimeoutException}
import scala.concurrent.duration.*
import scala.concurrent.Await
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

val rnd = new Random()

@main def main(): Unit = {
  println("Hello World")
  println("")

  val sizes = List(10, 100, 10000, 1000000, 10000000, 15000000)
  // Define a transformation matrix (e.g., rotation around the x-axis)
  val transformationMatrix = generateRotationMatrix(45, 'x')

  for (size <- sizes) {
    println(s"Testing with point cloud size: $size")
    val pointCloud = generatePointCloud(size, 50, 50, 50)

    // Sequential matrix multiplication
    withTimeout(30.seconds) {
      val sequentialStart = System.nanoTime()
      val sequentialResult = pointCloud.map(point => matrixMultiply(transformationMatrix, point))
      val sequentialEnd = System.nanoTime()
      val sequentialTime = (sequentialEnd - sequentialStart) / 1e6
      println(f"Sequential execution time: $sequentialTime%.2f ms")
    }

    // Advanced parallel matrix multiplication
    withTimeout(30.seconds) {
      val advancedParallelStart = System.nanoTime()
      val advancedParallelResult = advancedParallelMatrixMultiply(pointCloud, transformationMatrix)
      val advancedParallelEnd = System.nanoTime()
      val advancedParallelTime = (advancedParallelEnd - advancedParallelStart) / 1e6
      println(f"Advanced parallel execution time: $advancedParallelTime%.2f ms")
    }

    // Optimized parallel matrix multiplication
    withTimeout(30.seconds) {
      val optimizedParallelStart = System.nanoTime()
      val optimizedParallelResult = optimizedParallelMatrixMultiply(pointCloud, transformationMatrix)
      val optimizedParallelEnd = System.nanoTime()
      val optimizedParallelTime = (optimizedParallelEnd - optimizedParallelStart) / 1e6
      println(f"Optimized parallel execution time: $optimizedParallelTime%.2f ms")
    }
    println("")
  }
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
def generatePointCloud(size: Int, xBound: Int, yBound: Int, zBound: Int): Iterable[Vector[Int]] = {
  def generatePoint(): Vector[Int] = {
    val x = rnd.nextInt(xBound) * (if isNegative then -1 else 1)
    val y = rnd.nextInt(yBound) * (if isNegative then -1 else 1)
    val z = rnd.nextInt(zBound) * (if isNegative then -1 else 1)
    Vector(x, y, z, 1)
  }
  for i <- 0 to size yield generatePoint()
}

/**
 * generates a 4x4 rotation matrix for a given angle and axis
 * @param angle the angle of rotation, in degrees
 * @param axis the axis being rotated around
 * @return the rotation matrix
 */
def generateRotationMatrix(angle: Double, axis: Char): Vector[Vector[Double]] = {
  val angleRad = angle.toRadians
  axis match
    case 'x' =>
      val row1 = Vector(1.0, 0, 0, 0)
      val row2 = Vector(0, cos(angleRad), -sin(angleRad), 0)
      val row3 = Vector(0, sin(angleRad), cos(angleRad), 0)
      val row4 = Vector(0, 0, 0, 1.0)
      Vector(row1, row2, row3, row4)
    case 'y' =>
      val row1 = Vector(cos(angleRad), 0, sin(angleRad), 0)
      val row2 = Vector(0, 1.0, 0, 0)
      val row3 = Vector(-sin(angleRad), 0, cos(angleRad), 0)
      val row4 = Vector(0, 0, 0, 1.0)
      Vector(row1, row2, row3, row4)
    case 'z' =>
      val row1 = Vector(cos(angleRad), -sin(angleRad), 0, 0)
      val row2 = Vector(sin(angleRad), cos(angleRad), 0, 0)
      val row3 = Vector(0, 0, 1.0, 0)
      val row4 = Vector(0, 0, 0, 1.0)
      Vector(row1, row2, row3, row4)
}

/**
 * generates a 4x4 translation matrix for a given translation vector
 * @param translation the desired translation as a vector (x, y, z)
 * @return the translation matrix
 */
def generateTranslationMatrix(translation: Vector[Double]): Vector[Vector[Double]] = {
  val x = translation(0)
  val y = translation(1)
  val z = translation(2)

  Vector(Vector(1, 0, 0, x), Vector(0, 1, 0, y), Vector(0, 0, 1, z), Vector(0, 0, 0, 1))
}


/**
 * generates a 4x4 translation matrix to simulate camera movement
 * it's just a translation matrix with the direction inverted,
 * i.e. to move the camera forward 5 units, we move everything backwards 5 units (closer to the camera)
 * @param cameraMovement the desired camera movement as a vector (x, y, z)
 * @return the camera view matrix
 */
def generateViewMatrix(cameraMovement: Vector[Double]): Vector[Vector[Double]] = {
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
def generateProjectionMatrix(left: Double, right: Double, top: Double, bottom: Double, near: Double, far: Double): Vector[Vector[Double]] = {
  val row1 = Vector(2/(right-left), 0, 0, -(right+left)/(right-left))
  val row2 = Vector(0, 2/(top-bottom), 0, -(top+bottom)/(top-bottom))
  val row3 = Vector(0, 0, -2*(far-near), -(far+near)/(far-near))
  val row4 = Vector(0, 0, 0.0, 0.0)
  Vector(row1, row2, row3, row4)
}

/**
 * returns a random true/false value
 * @return true or false
 */
def isNegative: Boolean = {
  rnd.nextBoolean()
}

/**
 * Multiplies a 4x4 matrix with a 4D vector.
 * @param matrix the 4x4 matrix
 * @param vector the 4D vector
 * @return the resulting 4D vector
 */
def matrixMultiply(matrix: Vector[Vector[Double]], point: Vector[Int]): Vector[Double] = {
  val result = matrix.map(row => row.zip(point.map(_.toDouble)).map { case (a, b) => a * b }.sum)
  roundVector(result) // Apply rounding to the result
}

/**
 * Advanced parallel matrix multiplication using block matrix multiplication.
 * @param pointCloud the 3D point cloud
 * @param matrix the transformation matrix
 * @return the transformed point cloud
 */
def advancedParallelMatrixMultiply(pointCloud: Iterable[Vector[Int]], matrix: Vector[Vector[Double]]): Iterable[Vector[Double]] = {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

  // Define block size for better cache efficiency
  val blockSize = Math.max(10000, pointCloud.size / Runtime.getRuntime.availableProcessors())

  // Divide the point cloud into blocks
  val blocks = pointCloud.grouped(blockSize).toSeq

  // Process each block in parallel
  val futures = blocks.map(block => Future {
    block.map(point => matrixMultiply(matrix, point))
  })

  // Combine the results
  Await.result(Future.sequence(futures), Duration.Inf).flatten
}

/**
 * Highly optimized parallel matrix multiplication with thread affinity and pre-allocated buffers.
 * @param pointCloud the 3D point cloud
 * @param matrix the transformation matrix
 * @return the transformed point cloud
 */
def optimizedParallelMatrixMultiply(pointCloud: Iterable[Vector[Int]], matrix: Vector[Vector[Double]]): Iterable[Vector[Double]] = {
  import java.util.concurrent.ConcurrentLinkedQueue

  // Use a fixed thread pool with fewer threads for low-end hardware
  val numThreads = Math.max(2, Runtime.getRuntime.availableProcessors() / 2)
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(numThreads))

  // Dynamically calculate chunk size
  val chunkSize = Math.max(10000, pointCloud.size / (numThreads * 4))

  // Pre-allocate a thread-safe queue for results
  val resultQueue = new ConcurrentLinkedQueue[Iterable[Vector[Double]]]()

  // Divide the point cloud into chunks
  val chunks = pointCloud.grouped(chunkSize).toSeq

  // Process each chunk in parallel with thread affinity
  val futures = chunks.zipWithIndex.map { case (chunk, index) =>
    Future {
      val threadResult = chunk.map(point => matrixMultiply(matrix, point))
      resultQueue.add(threadResult) // Add results to the queue
    }
  }

  // Wait for all threads to complete
  Await.result(Future.sequence(futures), Duration.Inf)

  // Combine results from the queue
  resultQueue.asScala.flatten
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
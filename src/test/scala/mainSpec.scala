import org.scalatest.funsuite.AnyFunSuite

class mainSpec extends AnyFunSuite {

  test("Rotation matrix for 90 degrees around Z-axis produces correct transformation") {
    val rotationMatrix = generateRotationMatrix(90, 'z')
    val point = Array(1.0, 0.0, 0.0, 1.0)

    val result = rotationMatrix * point

    val expected = Array(0.0, 1.0, 0.0, 1.0)
    assert(result.zip(expected).forall { case (a, b) => Math.abs(a - b) < 1e-9 })
  }

  test("Point cloud transformation with composite matrix produces correct results") {
    val pointCloud = generatePointCloud(2, 10, 10, 10)
    val rotationMatrix = generateRotationMatrix(45, 'x')
    val translationMatrix = generateTranslationMatrix(Array(5.0, 5.0, 5.0))
    val compositeMatrix = rotationMatrix * translationMatrix

    val transformedPointCloud = pointCloud.map(point => compositeMatrix * point)

    assert(transformedPointCloud.nonEmpty)
    assert(transformedPointCloud.forall(_.length == 4))
  }

  test("Sequential and parallel methods produce the same results") {
    val pointCloud = generatePointCloud(1000, 50, 50, 50)
    val rotationMatrix = generateRotationMatrix(45, 'x')
    val transformationMatrix = generateTranslationMatrix(Array(3, 5, -3))
    val viewMatrix = generateViewMatrix(Array(6, -3, -2))
    val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix

    // Sequential result
    val sequentialResult = pointCloud.map(point => compositeMatrix * point)

    // Parallel result
    val parallelResult = pointCloudParallel(pointCloud.toArray, compositeMatrix)

    // Assert that results are approximately the same
    assert(areCollectionsEqual(sequentialResult.map(_.toVector), parallelResult.map(_.toVector)))
  }

  test("Methods handle empty point clouds correctly") {
    val pointCloud = Iterable.empty[Array[Double]]
    val rotationMatrix = generateRotationMatrix(45, 'x')
    val transformationMatrix = generateTranslationMatrix(Array(3, 5, -3))
    val viewMatrix = generateViewMatrix(Array(6, -3, -2))
    val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix

    // Sequential result
    val sequentialResult = pointCloud.map(point => compositeMatrix * point)

    // Parallel result
    val parallelResult = pointCloudParallel(pointCloud.toArray, compositeMatrix)

    // Assert that all results are empty
    assert(sequentialResult.isEmpty)
    assert(parallelResult.isEmpty)
  }

  test("Sequential and parallel methods with larger size produce the same results") {
    val pointCloud = generatePointCloud(10000, 50, 50, 50)
    val rotationMatrix = generateRotationMatrix(45, 'x')
    val transformationMatrix = generateTranslationMatrix(Array(3, 5, -3))
    val viewMatrix = generateViewMatrix(Array(6, -3, -2))
    val compositeMatrix = rotationMatrix * transformationMatrix * viewMatrix

    // Sequential result
    val sequentialResult = pointCloud.map(point => compositeMatrix * point)

    // Parallel result
    val parallelResult = pointCloudParallel(pointCloud.toArray, compositeMatrix)

    // Assert that results are approximately the same
    assert(areCollectionsEqual(sequentialResult.map(_.toVector), parallelResult.map(_.toVector)))
  }

  test("Matrix multiplication produces correct results for known inputs") {
    val pointCloud = Iterable(Array(1.0, 0.0, 0.0, 1.0), Array(0.0, 1.0, 0.0, 1.0))
    val transformationMatrix = generateRotationMatrix(90, 'z')

    // Expected results
    val expectedResults = roundCollection(Iterable(
      Array(0.0, 1.0, 0.0, 1.0).toVector,
      Array(-1.0, 0.0, 0.0, 1.0).toVector
    ))

    // Sequential result
    val sequentialResult = roundCollection(pointCloud.map(point => (transformationMatrix * point).toVector))

    // Assert correctness
    assert(sequentialResult == expectedResults)
  }

  test("Non-square matrix multiplication produces correct results") {
    val matrixA = new Matrix(Array(
      Array(1.0, 2.0, 3.0),
      Array(4.0, 5.0, 6.0)
    ))
    val matrixB = new Matrix(Array(
      Array(7.0, 8.0),
      Array(9.0, 10.0),
      Array(11.0, 12.0)
    ))

    val result = matrixA * matrixB

    val expected = Array(
      Array(58.0, 64.0),
      Array(139.0, 154.0)
    )

    assert(result.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

  test("Single-element matrix multiplication produces correct results") {
    val matrixA = new Matrix(Array(Array(2.0)))
    val matrixB = new Matrix(Array(Array(3.0)))

    val result = matrixA * matrixB

    val expected = Array(Array(6.0))

    assert(result.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

  test("Matrix multiplication with negative values produces correct results") {
    val matrixA = new Matrix(Array(
      Array(-1.0, -2.0),
      Array(-3.0, -4.0)
    ))
    val matrixB = new Matrix(Array(
      Array(5.0, 6.0),
      Array(7.0, 8.0)
    ))

    val result = matrixA * matrixB

    val expected = Array(
      Array(-19.0, -22.0),
      Array(-43.0, -50.0)
    )

    assert(result.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

  test("Large matrix multiplication produces correct results") {
    val size = 100
    val matrixA = new Matrix(Array.fill(size, size)(1.0))
    val matrixB = new Matrix(Array.fill(size, size)(2.0))

    val result = matrixA * matrixB

    val expected = Array.fill(size, size)(size * 2.0)

    assert(result.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

  test("Multiplication with a zero matrix produces a zero matrix") {
    val matrixA = new Matrix(Array(
      Array(1.0, 2.0),
      Array(3.0, 4.0)
    ))
    val zeroMatrix = new Matrix(Array(
      Array(0.0, 0.0),
      Array(0.0, 0.0)
    ))

    val result = matrixA * zeroMatrix

    val expected = Array(
      Array(0.0, 0.0),
      Array(0.0, 0.0)
    )

    assert(result.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

  test("Matrix rounding works correctly") {
    val matrix = new Matrix(Array(
      Array(1.12345, 2.98765),
      Array(3.54321, 4.67891)
    ))

    val roundedMatrix = matrix.rounded(2)

    val expected = Array(
      Array(1.12, 2.98),
      Array(3.54, 4.67)
    )

    assert(roundedMatrix.contents.zip(expected).forall { case (row1, row2) =>
      row1.sameElements(row2)
    })
  }

}
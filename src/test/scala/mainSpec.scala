import org.scalatest.funsuite.AnyFunSuite

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*
import scala.concurrent.Await

class MatrixMultiplicationTest extends AnyFunSuite {

  test("Sequential and parallel methods produce the same results") {
    val pointCloud = generatePointCloud(1000, 50, 50, 50)
    val transformationMatrix = generateRotationMatrix(45, 'x')

    // Sequential result
    val sequentialResult = pointCloud.map(point => matrixMultiply(transformationMatrix, point))

    // Advanced parallel result
    val advancedParallelResult = advancedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Optimized parallel result
    val optimizedParallelResult = optimizedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Assert that all results are approximately the same
    assert(areCollectionsEqual(sequentialResult, advancedParallelResult))
    assert(areCollectionsEqual(sequentialResult, optimizedParallelResult))
  }

  test("Methods handle empty point clouds correctly") {
    val pointCloud = Iterable.empty[Vector[Int]]
    val transformationMatrix = generateRotationMatrix(45, 'x')

    // Sequential result
    val sequentialResult = pointCloud.map(point => matrixMultiply(transformationMatrix, point))

    // Advanced parallel result
    val advancedParallelResult = advancedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Optimized parallel result
    val optimizedParallelResult = optimizedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Assert that all results are empty
    assert(sequentialResult.isEmpty)
    assert(advancedParallelResult.isEmpty)
    assert(optimizedParallelResult.isEmpty)
  }

  test("Sequential and parallel methods with larger size produce the same results") {
    val pointCloud = generatePointCloud(5000, 50, 50, 50)
    val transformationMatrix = generateRotationMatrix(45, 'x')

    // Sequential result
    val sequentialResult = pointCloud.map(point => matrixMultiply(transformationMatrix, point))

    // Advanced parallel result
    val advancedParallelResult = advancedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Optimized parallel result
    val optimizedParallelResult = optimizedParallelMatrixMultiply(pointCloud, transformationMatrix)

    // Assert that all results are approximately the same
    assert(areCollectionsEqual(sequentialResult, advancedParallelResult))
    assert(areCollectionsEqual(sequentialResult, optimizedParallelResult))
  }

  test("Matrix multiplication produces correct results for known inputs") {
    val pointCloud = Iterable(Vector(1, 0, 0, 1), Vector(0, 1, 0, 1))
    val transformationMatrix = generateRotationMatrix(90, 'z')
  
    // Expected results
    val expectedResults = roundCollection(Iterable(Vector(0.0, 1.0, 0.0, 1.0), Vector(-1.0, 0.0, 0.0, 1.0)))
  
    // Sequential result
    val sequentialResult = roundCollection(pointCloud.map(point => matrixMultiply(transformationMatrix, point)))
  
    // Assert correctness
    assert(sequentialResult == expectedResults)
  }
}
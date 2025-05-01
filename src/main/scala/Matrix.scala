import java.util.concurrent.Executors
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.collection.JavaConverters.collectionAsScalaIterableConverter

class Matrix(data: Array[Array[Double]]) {
  val numRows: Int = data.length
  val numCols: Int = if (numRows == 0) 0 else {
    data.head.length
  }
  val contents: Array[Array[Double]] = data

  def *(that: Matrix): Matrix = {
    require(this.numCols == that.numRows, s"Incompatible dimensions: ${this.numRows}x${this.numCols} cannot multiply ${that.numRows}x${that.numCols}")
    val resultRows: Array[Array[Double]] =
      Array.tabulate(this.numRows, that.numCols) { (i, j) =>
        // dot product of row i of this with column j of that
        (0 until this.numCols)
          .map(k => this.data(i)(k) * that.contents(k)(j))
          .sum
      }
    Matrix(resultRows)
  }

  def *(v: Array[Double]): Array[Double] = {
    require(this.numCols == v.length, s"Incompatible dimensions, ${this.numRows}x${this.numCols} cannot multiply vector of length ${v.length}")
    data.map(row => row.zip(v).map {case (a, b) => a*b}.sum)
  }

  def rounded(precision: Int = 3): Matrix = {
    val factor = math.pow(10, precision)
    def round(d: Double): Double = math.floor(d*factor)/factor
    val roundedData = data.map(row => row.map(round))
    Matrix(roundedData)
  }

  def parallelMultiply(that: Matrix): Matrix = {
      implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

      // Define block size for better cache efficiency
      val blockSize = Math.max(1, this.numRows / Runtime.getRuntime.availableProcessors())
      val blocks: Seq[Seq[Array[Double]]] = this.data.toSeq.grouped(blockSize).toSeq

      // Process each block in parallel
      val futures: Seq[Future[Matrix]] = blocks.map { rows =>
        Future {
          new Matrix(rows.toArray) * that
        }
      }

      val results: Seq[Matrix] = Await.result(Future.sequence(futures), Duration.Inf)

      val newRows: Array[Array[Double]] = results.foldLeft(Array.empty[Array[Double]]) { case (acc, mtx) =>
        acc ++ mtx.contents
      }
      new Matrix(newRows)
  }

  def optimizedParallelMultiply(that: Matrix): Matrix = {

    val n = this.numRows
    val m = this.numCols
    val p = that.numCols

    // pre-transpose B for ease of reference later: can go row by row instead of row x column
    val that_T = that.contents.transpose
    val cores = Runtime.getRuntime.availableProcessors()
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(cores))

    val chunkSize = Math.max(1, (n+cores-1) / cores)

    val chunks: Seq[Seq[Array[Double]]] = this.data.toSeq.grouped(chunkSize).toSeq

    val futures = (0 until n by chunkSize).map {
      startRow =>
        Future {
          val endRow = math.min(n, startRow + chunkSize)
          (startRow until endRow).map {
            i =>
              val rowA = this.data(i)
              val rowBuilder = Array.newBuilder[Double]
              var j = 0
              while (j < p) {
                var sum = 0.0
                var k = 0
                val colB = that_T(j)
                while (k < m) {
                  sum += rowA(k) * colB(k)
                  k += 1
                }
                rowBuilder += sum
                j += 1
              }
              (i, rowBuilder.result())
          }
        }
    }

    val results: Seq[(Int, Array[Double])] = Await.result(Future.sequence(futures), Duration.Inf).flatten
    val resultRows = results.sortBy(_._1).map(_._2).toArray
    new Matrix(resultRows)
  }

  override def toString: String = {
    val displayPrec = 3
    data.map { row =>
      row.map { x=>
        val factor = math.pow(10, displayPrec)
        val truncated = math.floor(x * factor)/factor
        f"${truncated}"
      }.mkString("[", ", ", "]")
    }.mkString("\n")
  }
}
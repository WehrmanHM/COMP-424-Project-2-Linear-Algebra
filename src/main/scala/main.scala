import scala.util.Random
import scala.math.{cos, sin}

val rnd = new Random()

@main def main(): Unit = {
  println("Hello World")
  val cloud = generatePointCloud(10, 50, 50, 50)
  for (point <- cloud) do println(point)
  println(generateRotationMatrix(127, 'x'))
}

def generatePointCloud(size: Int, xBound: Int, yBound: Int, zBound: Int): Iterable[Vector[Int]] = {
  def generatePoint(): Vector[Int] = {
    val x = rnd.nextInt(xBound) * (if isNegative then -1 else 1)
    val y = rnd.nextInt(yBound) * (if isNegative then -1 else 1)
    val z = rnd.nextInt(zBound) * (if isNegative then -1 else 1)
    Vector(x, y, z, 1)
  }
  for i <- 0 to size yield generatePoint()
}

// angle is in degrees
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

// translation is a vector of the form (x, y, z)
def generateTranslationMatrix(translation: Vector[Double]): Vector[Vector[Double]] = {
  val x = translation(0)
  val y = translation(1)
  val z = translation(2)

  Vector(Vector(1, 0, 0, x), Vector(0, 1, 0, y), Vector(0, 0, 1, z), Vector(0, 0, 0, 1))
}

// this is also how we make our view matrix, but we invert the direction
// i.e. to move the camera forward 5 units, we move everything backwards 5 units
// (closer to the camera)
def generateViewMatrix(cameraMovement: Vector[Double]): Vector[Vector[Double]] = {
  generateTranslationMatrix(cameraMovement.map(_ * -1))
}

def generateProjectionMatrix(left: Double, right: Double, top: Double, bottom: Double, near: Double, far: Double): Vector[Vector[Double]] = {
  val row1 = Vector(2/(right-left), 0, 0, -(right+left)/(right-left))
  val row2 = Vector(0, 2/(top-bottom), 0, -(top+bottom)/(top-bottom))
  val row3 = Vector(0, 0, -2*(far-near), -(far+near)/(far-near))
  val row4 = Vector(0, 0, 0.0, 0.0)
  Vector(row1, row2, row3, row4)
}

def isNegative: Boolean = {
  rnd.nextBoolean()
}
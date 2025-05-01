import scala.util.Random
import scala.math.{cos, sin}

val rnd = new Random()

@main def main(): Unit = {
  println("Hello World")
  val cloud = generatePointCloud(10, 50, 50, 50)
  for (point <- cloud) do println(point)
  println(generateRotationMatrix(127, 'x'))
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
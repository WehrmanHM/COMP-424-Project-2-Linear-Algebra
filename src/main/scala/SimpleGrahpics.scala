class SimpleGrahpics

// The "Image" DSL is the easiest way to create images
import doodle.image.*
// Colors and other useful stuff
import doodle.core.*
// Extension methods
import doodle.image.syntax.all.*
// Render to a window using Java2D (must be running in the JVM)
import doodle.java2d.*
// Need the Cats Effect runtime to run everything
import cats.effect.unsafe.implicits.global

import scala.io.StdIn.readLine

val blackSquare = Image.rectangle(30, 30).fillColor(Color.black)
val redSquare = Image.rectangle(30, 30).fillColor(Color.red)

// A chessboard, broken into steps showing the recursive construction
val twoByTwo =
  (redSquare.beside(blackSquare))
    .above(blackSquare.beside(redSquare))

val fourByFour =
  (twoByTwo.beside(twoByTwo))
    .above(twoByTwo.beside(twoByTwo))

val chessboard =
  (fourByFour.beside(fourByFour))
    .above(fourByFour.beside(fourByFour))

@main
def draw(): Unit = {
  var moveText = ""
  while (true) {
    moveText = readLine()
    println(s"Cube will rotate to the $moveText")

    chessboard.draw()
  }
}

class PointH(val x: Double, val y: Double, val z: Double) {

  var pArray: Array[Double] = Array();;
  
  def this(x: Double, y: Double, z: Double, pArray: Array[Double]) = {
    this(x, y, z)
    this.pArray = Array(x, y, z, 1.0)
  }

  def getPoint(): Array[Double] = {
    pArray
  }

}

class Cube (val sideLength: Double, val x: Double, val y: Double) {


  var point1 = PointH(sideLength / 2 + x, sideLength / 2 + y, 0)
  var point2 = PointH(-sideLength / 2 + x, sideLength / 2 + y, 0)
  var point3 = PointH(sideLength / 2 + x, -sideLength / 2 + y, 0)
  var point4 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, 0)
  var point5 = PointH(sideLength / 2 + x, sideLength / 2 + y, sideLength)
  var point6 = PointH(-sideLength / 2 + x, sideLength / 2 + y, sideLength)
  var point7 = PointH(sideLength / 2 + x, -sideLength / 2 + y, sideLength)
  var point8 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, sideLength)
  var pArray = Array(point1, point2, point3, point4, point5, point6, point8)
}

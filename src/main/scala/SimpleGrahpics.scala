//class SimpleGrahpics
//
//// The "Image" DSL is the easiest way to create images
//import doodle.image.{Image, *}
//// Colors and other useful stuff
//import doodle.core.*
//// Extension methods
//import doodle.image.syntax.all.*
//// Render to a window using Java2D (must be running in the JVM)
//import doodle.java2d.*
//// Need the Cats Effect runtime to run everything
//import cats.effect.unsafe.implicits.global
//
//import scala.io.StdIn.readLine
//
//import doodle.syntax.all.LayoutPictureOps
//import doodle.syntax.layout.LayoutPictureOps
//import doodle.syntax.all.StylePictureOps
//import doodle.syntax.style.StylePictureOps
//import doodle.syntax.all.RendererPictureOps
//import doodle.syntax.renderer.RendererPictureOps
//
//val feather =
//  ClosedPath.empty
//  .lineTo(100, 100)
//  .curveTo(90, 75, 90, 25, 10, 10)
//  .moveTo(100, 100)
//  .curveTo(75, 90, 25, 90, 10, 10)//.strokeColor(Color.red).strokeWidth(5.0)
//
//@main
//def draw(): Unit = {
//  var moveText = ""
//
//  var c = Cube(100, 0, 0)
//
//  while (true) {
//    //moveText = readLine()
//    println(s"Cube will rotate to the $moveText")
//    c.move()
//    c.displayC()
//  }
//}
//
//class PointH(val x: Double, val y: Double, val z: Double) {
//
//  var pArray: Array[Double] = Array();;
//
//  def this(x: Double, y: Double, z: Double, pArray: Array[Double]) = {
//    this(x, y, z)
//    this.pArray = Array(x, y, z, 1.0)
//  }
//
//  def getPoint(): Array[Double] = {
//    pArray
//  }
//
//}
//
//class Cube(val sideLength: Double, val x: Double, val y: Double) {
//
//  var point1: PointH = PointH(1,1,1)
//  var point2: PointH = PointH(1,1,1)
//  var point3: PointH = PointH(1,1,1)
//  var point4: PointH = PointH(1,1,1)
//  var point5: PointH = PointH(1,1,1)
//  var point6: PointH = PointH(1,1,1)
//  var point7: PointH = PointH(1,1,1)
//  var point8: PointH = PointH(1,1,1)
//
//  var pArray: Array[PointH] = Array()
//
//  this.point1 = PointH(sideLength / 2 + x, sideLength / 2 + y, 0)
//  this.point2 = PointH(-sideLength / 2 + x, sideLength / 2 + y, 0)
//  this.point3 = PointH(sideLength / 2 + x, -sideLength / 2 + y, 0)
//  this.point4 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, 0)
//  this.point5 = PointH(sideLength / 2 + x, sideLength / 2 + y, sideLength)
//  this.point6 = PointH(-sideLength / 2 + x, sideLength / 2 + y, sideLength)
//  this.point7 = PointH(sideLength / 2 + x, -sideLength / 2 + y, sideLength)
//  this.point8 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, sideLength)
//  this.pArray = Array(point1, point2, point3, point4, point5, point6, point8)
//
//  /*def this(sideLength: Double, x: Double, y: Double) = {
//    this(sideLength, x, y)
//
//    this.point1 = PointH(sideLength / 2 + x, sideLength / 2 + y, 0)
//    this.point2 = PointH(-sideLength / 2 + x, sideLength / 2 + y, 0)
//    this.point3 = PointH(sideLength / 2 + x, -sideLength / 2 + y, 0)
//    this.point4 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, 0)
//    this.point5 = PointH(sideLength / 2 + x, sideLength / 2 + y, sideLength)
//    this.point6 = PointH(-sideLength / 2 + x, sideLength / 2 + y, sideLength)
//    this.point7 = PointH(sideLength / 2 + x, -sideLength / 2 + y, sideLength)
//    this.point8 = PointH(-sideLength / 2 + x, -sideLength / 2 + y, sideLength)
//    this.pArray = Array(point1, point2, point3, point4, point5, point6, point8)
//  }*/
//
//  def move(): Unit = {
//    for p <- pArray do {
//      // TODO: matrix multiplication here
//    }
//  }
//
//  def displayC(): Unit = {
//    val face1 = ClosedPath.empty
//      .moveTo(point1.y, Math.sqrt(2)/2 * (point1.x-point1.z))
//      .lineTo(point2.y, Math.sqrt(2)/2 * (point2.x-point2.z))
//      .lineTo(point4.y, Math.sqrt(2)/2 * (point4.x-point4.z))
//      .lineTo(point3.y, Math.sqrt(2)/2 * (point3.x-point3.z))
//      .lineTo(point1.y, Math.sqrt(2)/2 * (point1.x-point1.z))//.strokeColor(Color.red).strokeWidth(5.0)
//
//    val face2 = ClosedPath.empty
//      .moveTo(point1.y, Math.sqrt(2)/2 * (point1.x - point1.z))
//      .lineTo(point2.y, Math.sqrt(2)/2 * (point2.x - point2.z))
//      .lineTo(point6.y, Math.sqrt(2)/2 * (point6.x - point6.z))
//      .lineTo(point5.y, Math.sqrt(2)/2 * (point5.x - point5.z))
//      .lineTo(point1.y, Math.sqrt(2)/2 * (point1.x - point1.z))
//
//    val face3 = ClosedPath.empty
//      .moveTo(point1.y, Math.sqrt(2) / 2 * (point1.x - point1.z))
//      .lineTo(point3.y, Math.sqrt(2) / 2 * (point3.x - point3.z))
//      .lineTo(point7.y, Math.sqrt(2) / 2 * (point7.x - point7.z))
//      .lineTo(point5.y, Math.sqrt(2) / 2 * (point5.x - point5.z))
//      .lineTo(point1.y, Math.sqrt(2) / 2 * (point1.x - point1.z))
//
//    val face4 = ClosedPath.empty
//      .moveTo(point4.y, Math.sqrt(2) / 2 * (point4.x - point4.z))
//      .lineTo(point3.y, Math.sqrt(2) / 2 * (point3.x - point3.z))
//      .lineTo(point7.y, Math.sqrt(2) / 2 * (point7.x - point7.z))
//      .lineTo(point8.y, Math.sqrt(2) / 2 * (point8.x - point8.z))
//      .lineTo(point4.y, Math.sqrt(2) / 2 * (point4.x - point4.z))
//
//    val face5 = ClosedPath.empty
//      .moveTo(point4.y, Math.sqrt(2) / 2 * (point4.x - point4.z))
//      .lineTo(point2.y, Math.sqrt(2) / 2 * (point2.x - point3.z))
//      .lineTo(point6.y, Math.sqrt(2) / 2 * (point6.x - point7.z))
//      .lineTo(point8.y, Math.sqrt(2) / 2 * (point8.x - point8.z))
//      .lineTo(point4.y, Math.sqrt(2) / 2 * (point4.x - point4.z))
//
//    val face6 = ClosedPath.empty
//      .moveTo(point5.y, Math.sqrt(2)/2 * (point5.x - point5.z))
//      .lineTo(point6.y, Math.sqrt(2)/2 * (point6.x - point6.z))
//      .lineTo(point8.y, Math.sqrt(2)/2 * (point8.x - point8.z))
//      .lineTo(point7.y, Math.sqrt(2)/2 * (point7.x - point7.z))
//      .lineTo(point5.y, Math.sqrt(2)/2 * (point5.x - point5.z))
//
//    val face1Image = Picture.path(face1)
//    val face2Image = Picture.path(face2)
//    val face3Image = Picture.path(face2)
//    val face4Image = Picture.path(face2)
//    val face5Image = Picture.path(face2)
//    val face6Image = Picture.path(face6).strokeColor(Color.red)
//
//    val cubeImage = face6Image.at(0,0).on(face5Image.at(0,0)).on(face4Image.at(0,0))
//      .on(face3Image.at(0,0)).on(face2Image.at(0,0)).on(face1Image.at(0,0))
//
//    cubeImage.draw()
//  }
//}

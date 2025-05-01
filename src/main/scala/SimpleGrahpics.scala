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
  while (true) {
    chessboard.draw()
  }
}

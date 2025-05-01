ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.3"

lazy val root = (project in file("."))
  .settings(
    name := "COMP 424 Project 2 Linear Algebra"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

Test / parallelExecution := false

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"
libraryDependencies ++= Seq("org.jocl" % "jocl" % "2.0.0")

libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "org.openjfx" % "javafx-base"     % "21" classifier "win",
  "org.openjfx" % "javafx-graphics" % "21" classifier "win",
  "org.openjfx" % "javafx-controls" % "21" classifier "win"
)

Compile / run / javaOptions ++= {
  val javafxJars = (Compile / dependencyClasspath).value.files
    .filter(f => f.getName.startsWith("javafx-"))
    .map(_.getParentFile.getAbsolutePath)
    .distinct
    .mkString(";")
  Seq(
    "--module-path", javafxJars,
    "--add-modules", "javafx.controls,javafx.graphics"
  )
}

libraryDependencies += "org.creativescala" %% "doodle" % "0.30.0"

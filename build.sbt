ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.4.3"

lazy val root = (project in file("."))
  .settings(
    name := "COMP 424 Project 2 Linear Algebra"
  )

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.19" % Test

Test / parallelExecution := false

libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.2.0"

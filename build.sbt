import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "spoter.me",
      scalaVersion := "2.12.5",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "KGV Prototype",
    scalacOptions += "-feature",
    scalaJSUseMainModuleInitializer := true,
    // creates single js resource file for easy integration in html page
    skip in packageJSDependencies := false,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.3.1",
      scalaTest % Test
    ),
    npmDependencies in Compile ++= Seq(
      "react" -> "16.5.1",
      "react-dom" -> "16.5.1")
  ).enablePlugins(ScalaJSPlugin, ScalaJSWeb, ScalaJSBundlerPlugin)

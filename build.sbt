import Dependencies._

val reactVersion = "16.5.1"
val scalaJSReactVersion = "1.3.1"
val scalaCssVersion = "0.5.5"

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
    version in webpack := "4.28.1",
    version in startWebpackDevServer := "3.1.14",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.5",
      "com.github.japgolly.scalajs-react" %%% "core" % scalaJSReactVersion,
      "com.github.japgolly.scalajs-react" %%% "extra" % scalaJSReactVersion,
      "com.github.japgolly.scalacss" %%% "core" % scalaCssVersion,
      "com.github.japgolly.scalacss" %%% "ext-react" % scalaCssVersion,
      "org.scala-js" %%% "scalajs-java-time" % "0.2.5",
      "com.beachape" %%% "enumeratum" % "1.5.13",
      scalaTest % Test
    ),
    npmDependencies in Compile ++= Seq(
      "react" -> reactVersion,
      "react-dom" -> reactVersion,
      "@solid/react" -> "1.1.3"),
  ).enablePlugins(ScalaJSPlugin, ScalaJSBundlerPlugin)

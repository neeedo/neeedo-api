name := """api"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "1.4.2",
  "com.softwaremill.macwire" %% "macros" % "0.7.3",
  "com.softwaremill.macwire" %% "runtime" % "0.7.3",
  "io.sphere.sdk.jvm" % "models" % "1.0.0-M9",
  "io.sphere.sdk.jvm" %% "play-2_3-java-client" % "1.0.0-M9"
)
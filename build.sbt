import com.typesafe.sbt.SbtNativePackager._
import NativePackagerKeys._
import com.typesafe.sbt.packager.archetypes.ServerLoader.SystemV

name := """neeedo-api"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

maintainer in Linux := "neeedo-team <neeedo@neeedo.com>"

packageSummary in Linux := "neeedo api application"

serverLoading in Linux := SystemV

packageDescription := "neeedo api application"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

excludeFilter in (Compile, unmanagedResources) := "es-data"

unmanagedResourceDirectories in Test += baseDirectory.value / "test"

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "1.4.2",
  "com.softwaremill.macwire" %% "macros" % "0.7.3",
  "com.softwaremill.macwire" %% "runtime" % "0.7.3",
  "io.sphere.sdk.jvm" % "models" % "1.0.0-M9",
  "io.sphere.sdk.jvm" %% "play-2_3-java-client" % "1.0.0-M9"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value // run the previous initialization
    if (sys.props("java.specification.version") < "1.8") {
      sys.error("Java 8 is required for this project.")
    }
}

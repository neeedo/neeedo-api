import com.typesafe.sbt.packager.archetypes.ServerLoader.SystemV
import com.typesafe.sbt.packager.debian.DebianPlugin
import play.PlayImport.PlayKeys._

name := """neeedo-api"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.1"

maintainer in Debian := "neeedo-team <neeedo@neeedo.com>"

packageSummary in Debian := "neeedo api application"

serverLoading in Debian := SystemV

packageDescription := "neeedo api application"

lazy val root = (project in file(".")).enablePlugins(PlayScala, DebianPlugin)

unmanagedResourceDirectories in Compile += baseDirectory.value / "resources"

excludeFilter in (Compile, unmanagedResources) := "es-data"

unmanagedResourceDirectories in Test += baseDirectory.value / "test"

libraryDependencies ++= Seq(
  "org.elasticsearch" % "elasticsearch" % "1.5.0",
  "com.softwaremill.macwire" %% "macros" % "0.7.3",
  "com.softwaremill.macwire" %% "runtime" % "0.7.3",
  "io.sphere.sdk.jvm" % "sphere-models" % "1.0.0-M14",
  "io.sphere.sdk.jvm" % "sphere-scala-client_2.11" % "1.0.0-M14",
  "com.amazonaws" % "aws-java-sdk" % "1.9.6",
  cache,
  ws
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

initialize := {
  val _ = initialize.value // run the previous initialization
    if (sys.props("java.specification.version") < "1.8") {
      sys.error("Java 8 is required for this project.")
    }
}

javaOptions in Test +="-Dlogger.resource=test-logger.xml"
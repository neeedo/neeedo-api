resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.8")

// web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

// code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.0.0")
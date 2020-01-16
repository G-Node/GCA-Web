// Comment to get more information during initialization
logLevel := Level.Warn

// Typesafe repository and required updated maven repository
resolvers ++= Seq("Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/",
                  "new maven" at "https://repo1.maven.org/maven2/")

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.10")

// SBT web plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.4")

// TODO clean up js code until it passes jshint and uncomment next line
// addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.7")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.1.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

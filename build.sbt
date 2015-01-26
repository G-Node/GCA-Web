
name := "GCA-Web"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "org.scalatest" %% "scalatest" % "2.2.1",
  "org.eclipse.persistence" % "org.eclipse.persistence.jpa" % "2.5.2",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.0",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4",
  // web jars
  "org.webjars" % "requirejs" % "2.1.15",
  "org.webjars" % "jquery" % "1.11.2",
  "org.webjars" % "jquery-ui" % "1.11.2",
  "org.webjars" % "bootstrap" % "3.3.1" exclude("org.webjars", "jquery"),
  "org.webjars" % "knockout" % "3.2.0" exclude("org.webjars", "jquery"),
  "org.webjars" % "sammy" % "0.7.4",
  "org.webjars" % "momentjs" % "2.9.0"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

pipelineStages := Seq(rjs, digest)
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
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

name := "GCA-Web"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.scalatest" %% "scalatest" % "2.0",
  "ws.securesocial" %% "securesocial" % "2.1.4",
  "org.eclipse.persistence" % "org.eclipse.persistence.jpa" % "2.5.1",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"
)

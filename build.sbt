import play.Project._

name := "GCA-Web"

version := "1.0"

playScalaSettings

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  "org.scalatest" % "scalatest_2.10" % "2.0",
  "ws.securesocial" %% "securesocial" % "2.1.3",
  "org.eclipse.persistence" % "org.eclipse.persistence.jpa" % "2.5.1",
  "org.postgresql" % "postgresql" % "9.3-1100-jdbc4"
)

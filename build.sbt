import play.Project._

name := "GCA-Web"

version := "1.0"

playScalaSettings

libraryDependencies ++= Seq(
  javaJpa,
  javaJpa.exclude("org.hibernate.javax.persistence", "hibernate-jpa-2.0-api"),
  "ws.securesocial" %% "securesocial" % "2.1.3",
  "org.hibernate" % "hibernate-entitymanager" % "4.3.0.Final"
)

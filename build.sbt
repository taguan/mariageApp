name := """mariageApp"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.google.inject" % "guice" % "3.0",
  "mysql" % "mysql-connector-java" % "5.1.31",
  "org.scalatestplus" %% "play" % "1.2.0" % "test",
  "com.typesafe.play.plugins" %% "play-plugins-mailer" % "2.3.1"
)

emberJsVersion := "1.5.1"

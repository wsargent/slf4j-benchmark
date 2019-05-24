import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "slf4j-benchmark",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.3",
    addCommandAlias("jmhRun",
      s""";jmh:run -i 20 -wi 10 -f1 -t1""".stripMargin)
  ).enablePlugins(JmhPlugin)


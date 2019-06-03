import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.tersesystems"
ThisBuild / organizationName := "tersesystems"

lazy val root = (project in file("."))
  .settings(
    name := "slf4jbench",
    addCommandAlias("jmhRun",
      s""";jmh:run -i 20 -wi 10 -f1 -t1""".stripMargin),
    addCommandAlias("jmhLatencyRun",
      s""";jmh:run -rf text -rff latency.txt -i 20 -wi 10 -f1 -t1 .*SLF4JBenchmark.*""".stripMargin),
    addCommandAlias("jmhThroughputRun",
      s""";jmh:run -i 20 -wi 10 -f1 -t1 .*AppenderBenchmark""".stripMargin)
  )
  .aggregate(`slf4jbench-logback`)
  .aggregate(`slf4jbench-log4j2`)


lazy val `slf4jbench-logback` = (project in file("logback"))
  .settings(
    name := "slf4jbench-logback",
    resolvers += Resolver.jcenterRepo,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "5.3",
    libraryDependencies += "com.sizmek.fsi" %% "fsi-macros" % "0.5.0",

  ).enablePlugins(JmhPlugin)

lazy val `slf4jbench-log4j2` = (project in file("log4j2"))
  .settings(
    name := "slf4jbench-log4j2",
    resolvers += Resolver.jcenterRepo,
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.11.2",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.2",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.11.2",
    libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.11.2" classifier "tests",
    libraryDependencies += "com.sizmek.fsi" %% "fsi-macros" % "0.5.0",
    addCommandAlias("jmhRun",
      s""";jmh:run -i 20 -wi 10 -f1 -t1""".stripMargin)
  ).enablePlugins(JmhPlugin)


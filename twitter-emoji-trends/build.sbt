name := "twitter-emoji-trends"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.twitter4j" % "twitter4j-stream" % "4.0.6",
  "com.vdurmont" % "emoji-java" % "4.0.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.6",
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.6",
  "net.debasishg" %% "redisclient" % "3.4",
  "net.codingwell" %% "scala-guice" % "4.1.0"
)
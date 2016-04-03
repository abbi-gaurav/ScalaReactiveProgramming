name := "PrinciplesOfReactiveProgramming"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.9.2"

libraryDependencies += "io.reactivex" % "rxscala_2.11" % "0.24.1"

libraryDependencies +=  "com.typesafe.akka" %% "akka-actor" % "2.4.0"

libraryDependencies += "com.ning" % "async-http-client" % "1.8.8"

libraryDependencies += "org.jsoup" % "jsoup" % "1.8.1"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-testkit_2.11" % "2.3.9" % "test"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.2" % "test"

libraryDependencies += "com.typesafe.akka" % "akka-persistence_2.11" % "2.4.0"

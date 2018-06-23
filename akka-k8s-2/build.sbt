name := "akka-k8s-2"

version := "0.1"

scalaVersion := "2.12.6"

resolvers += Resolver.bintrayRepo("tanukkii007", "maven")

enablePlugins(JavaServerAppPackaging, DockerPlugin)

val akkaVersion = "2.5.12"
val akkaHttpVersion = "10.1.1"
val akkaManagementVersion = "0.13.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
  "com.lightbend.akka.discovery" %% "akka-discovery-dns" % akkaManagementVersion,
  "com.github.TanUkkii007" %% "akka-cluster-custom-downing" % "0.0.12"
)

dockerBaseImage := "openjdk:8"

dockerUsername := Some("gabbi")
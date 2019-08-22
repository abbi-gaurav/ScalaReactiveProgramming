import com.typesafe.sbt.packager.docker._

enablePlugins(JavaServerAppPackaging)

name := "akka-bootstrap-k8s-dns"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % "1.0.0",
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.lightbend.akka.management" %% "akka-management-cluster-http" % "1.0.0"
)

dockerCommands :=
  dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args@_*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  }

dockerExposedPorts := Seq(8080, 8558, 2552)
dockerBaseImage := "openjdk:8-jre-alpine"

daemonUserUid in Docker := None
daemonUser in Docker    := "daemon"

dockerCommands ++= Seq(
  Cmd("USER", "root"),
  Cmd("RUN", "/sbin/apk", "add", "--no-cache", "bash", "bind-tools", "busybox-extras", "curl", "strace"),
  Cmd("RUN", "chgrp -R 0 . && chmod -R g=u .")
)
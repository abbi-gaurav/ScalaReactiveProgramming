import com.trueaccord.scalapb.compiler.Version.scalapbVersion
import com.trueaccord.scalapb.compiler.Version.grpcJavaVersion

name := "explore-grpc"

version := "0.1"

scalaVersion := "2.12.4"

// add these ScalaPB settings to your current settings
PB.protoSources.in(Compile) := Seq(sourceDirectory.in(Compile).value / "proto")

PB.targets.in(Compile) := Seq(scalapb.gen() -> sourceManaged.in(Compile).value)

libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,
  "io.grpc" % "grpc-netty" % grpcJavaVersion
)
        
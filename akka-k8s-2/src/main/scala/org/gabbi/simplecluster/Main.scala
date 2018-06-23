package org.gabbi.simplecluster

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.http.scaladsl.Http
import akka.management.AkkaManagement
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext

object Main extends App {
  private lazy val config = ConfigFactory.load()
  private implicit val system: ActorSystem = ActorSystem("akka-simple-cluster")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher
  private implicit val cluster: Cluster = Cluster(system)

  AkkaManagement(system).start()
  ClusterBootstrap(system).start()

  Http().bindAndHandle(
    complete(config.getString("application.api.hello-msg")),
    config.getString("application.api.host"),
    config.getInt("application.api.port")
  )
}

package akka.cluster.bootstrap

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props}
import akka.cluster.ClusterEvent.ClusterDomainEvent
import akka.cluster.{Cluster, ClusterEvent, ClusterRemoteWatcher}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings}
import akka.http.scaladsl.Http
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContextExecutor

object ClusterApp {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val cluster = Cluster(system)

    system.log.info("Starting Akka Management")
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    system.actorOf(
      ClusterSingletonManager.props(
        Props[NoisySingleton],
        PoisonPill,
        ClusterSingletonManagerSettings(system)
      )
    )

    Cluster(system).subscribe(
      system.actorOf(Props[ClusterWatcher]),
      ClusterEvent.InitialStateAsEvents,
      classOf[ClusterDomainEvent]
    )

    val routes =
      path("hello") {
        get {
          complete(
            HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Hello<h1>")
          )
        }
      }

    Http().bindAndHandle(routes, "0.0.0.0", 8080)

    system.log.info(
      s"Server is online at http://localhost:8080/\n Press return to stop..."
    )

    cluster.registerOnMemberUp(() => {
      system.log.info("Cluster member is up")
    })
  }

  class ClusterWatcher extends Actor with ActorLogging {
    private val cluster = Cluster(context.system)

    override def receive: Receive = {
      case msg => log.info(s"Cluster ${cluster.selfAddress} >>> " + msg)
    }
  }

}

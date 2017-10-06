package api.common

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

trait StartUp {

  def startUp(api: Route)(implicit actorSystem: ActorSystem): Unit = {
    val host = actorSystem.settings.config.getString("http.host")
    val port = actorSystem.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)(implicit actorSystem: ActorSystem): Unit = {
    implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
    implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
    val bindingFuture: Future[ServerBinding] = Http().bindAndHandle(api, host, port)

    val log: LoggingAdapter = Logging(actorSystem.eventStream, "gabbi-ticks")
    bindingFuture.onComplete {
      case Success(serverBinding) => log.info(s"RestApi bound to ${serverBinding.localAddress} ")
      case Failure(throwable) =>
        log.error(throwable, s"failed to bind to $host : $port")
        actorSystem.terminate()
    }
  }
}

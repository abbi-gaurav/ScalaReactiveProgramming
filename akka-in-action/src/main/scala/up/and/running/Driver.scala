package up.and.running

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Driver extends App with RequestTimeout {
  private val config = ConfigFactory.load("up-and-running.conf")
  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")

  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher

  private val api: Route = new RestApi(actorSystem, requestTimeout(config)).routes
  private implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  private val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(api, host, port)

  private val log: LoggingAdapter = Logging(actorSystem.eventStream, "gabbi-ticks")
  bindingFuture.onComplete {
    case Success(serverBinding) => log.info(s"RestApi bound to ${serverBinding.localAddress} ")
    case Failure(throwable) =>
      log.error(throwable, s"failed to bind to $host : $port")
      actorSystem.terminate()
  }
}

trait RequestTimeout {

  import scala.concurrent.duration._

  def requestTimeout(config: Config): Timeout = {
    val t = config.getString("akka.http.server.request-timeout")
    val d = Duration(t)
    FiniteDuration(d.length, d.unit)
  }
}

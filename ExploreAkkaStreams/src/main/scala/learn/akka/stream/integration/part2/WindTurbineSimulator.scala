package learn.akka.stream.integration.part2

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.model.StatusCode
import akka.stream.ActorMaterializer
import learn.akka.stream.integration.part2.WindTurbineSimulator._

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by gabbi on 25.06.17.
  */
object WindTurbineSimulator {
  def props(id: String, endpoint: String)(implicit materializer: ActorMaterializer) =
    Props(classOf[WindTurbineSimulator], id, endpoint, materializer)

  final case object Upgraded

  final case object Connected

  final case object Terminated

  final case class ConnectionFailure(ex: Throwable)

  final case class FailedUpgrade(statusCode: StatusCode)

}

class WindTurbineSimulator(id: String, endpoint: String)(implicit materializer: ActorMaterializer) extends Actor with ActorLogging {
  private implicit val system: ActorSystem = context.system
  private implicit val executionContext: ExecutionContextExecutor = context.dispatcher

  private val webSocketClient: WebSocketClient = WebSocketClient(id, endpoint, self)

  override def postStop() = {
    log.info(s"$id : Stopping WebSocket connection")
    webSocketClient.killSwitch.shutdown()
  }

  override def receive: Receive = {
    case Upgraded => log.info(s"id: WS upgraded")
    case ConnectionFailure(ex) =>
      log.info(s"failed to connect $id WS")
      throw new WindTurbineSimulatorException(id)
    case FailedUpgrade(statusCode) =>
      log.info(s"failed to upgrade $id WS: due to $statusCode")
    case Connected =>
      log.info(s"$id WS connected")
      context.become(running)
  }

  private def running: Receive = {
    case Terminated =>
      throw new WindTurbineSimulatorException(id)
      log.info(s"$id WS terminated")
  }
}

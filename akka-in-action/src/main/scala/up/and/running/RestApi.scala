package up.and.running

import akka.actor.{ActorRef, ActorSystem}
import akka.util.Timeout
import api.common.RestRoutes
import api.common.actors.BoxOffice

import scala.concurrent.ExecutionContextExecutor

class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  override def createBoxOffice(): ActorRef = actorSystem.actorOf(BoxOffice.props, BoxOffice.name)
}




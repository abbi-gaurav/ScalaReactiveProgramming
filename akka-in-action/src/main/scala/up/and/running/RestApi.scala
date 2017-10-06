package up.and.running

import akka.actor.ActorRef
import akka.util.Timeout
import api.common.RestRoutes

import scala.concurrent.ExecutionContext

class RestApi(ec: ExecutionContext, timeout: Timeout)(boxOfficeCreator: => ActorRef) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContext = ec

  override def createBoxOffice(): ActorRef = boxOfficeCreator
}




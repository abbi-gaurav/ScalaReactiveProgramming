package scenarios

import akka.actor.{Actor, ActorRef}
import scenarios.SilentActor.{GetState, SilentMessage}

class SilentActor extends Actor {
  private var internalState = Vector.empty[String]
  override def receive: Receive = {
    case SilentMessage(str) => internalState = internalState :+ str
    case GetState(receiver) => receiver ! internalState
  }
}

object SilentActor {

  case class SilentMessage(data: String)

  case class GetState(receiver: ActorRef)

}

package scenarios

import akka.actor.{Actor, ActorRef, Props}

object FilteringActor {
  def props(nextActor: ActorRef, bufferSize: Int) = Props(new FilteringActor(nextActor, bufferSize))

  case class Event(id: Long)

}

class FilteringActor(nextActor: ActorRef, bufferSize: Long) extends Actor {

  import FilteringActor._

  private var lastMessages = Vector[Event]()

  override def receive: Receive = {
    case event: Event =>
      if (!lastMessages.contains(event)) {
        lastMessages = lastMessages :+ event
        nextActor ! event

        if (lastMessages.size > bufferSize) {
          lastMessages = lastMessages.tail
        }
      }
  }
}

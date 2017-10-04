package scenarios

import akka.actor.{Actor, ActorRef, Props}
import scenarios.SendingActor.{SortEvents, SortedEvents}

object SendingActor {
  def props(receiver: ActorRef) = Props(new SendingActor(receiver))

  case class Event(id: Long)

  case class SortEvents(unsorted: Vector[Event])

  case class SortedEvents(sorted: Vector[Event])

}

class SendingActor(receiver: ActorRef) extends Actor {
  override def receive: Receive = {
    case SortEvents(unsorted) => receiver ! SortedEvents(unsorted.sortBy(_.id))
  }
}

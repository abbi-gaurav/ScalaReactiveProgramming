package w5.urlFetcher

import akka.actor.{Actor, ActorRef, Props}

/**
 * Created by gabbi on 23/06/15.
 */
class StepParent(child: Props, probe: ActorRef) extends Actor {
  context.actorOf(child)

  override def receive: Receive = {
    case msg => probe.tell(msg, sender())
  }
}

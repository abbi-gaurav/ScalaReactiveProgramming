package w5.testing

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
 * Created by gabbi on 23/06/15.
 */
class Toggle extends Actor{
  private def happy:Receive = {
    case "How are you" =>
      sender() ! "happy"
      context become sad

  }

  private def sad:Receive = {
    case "How are you" =>
      sender() ! "sad"
      context become happy
  }
  override def receive: Receive = happy
}

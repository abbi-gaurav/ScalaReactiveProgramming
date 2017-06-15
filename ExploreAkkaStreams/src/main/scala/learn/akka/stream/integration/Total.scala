package learn.akka.stream.integration

import akka.Done
import akka.actor.Actor
import learn.akka.stream.integration.Total.{CurrentTotal, GetTotal, Increment}

/**
  * Created by gabbi on 23.04.17.
  */
class Total extends Actor {
  var total: Long = 0

  override def receive: Receive = {
    case Increment(value) =>
      total += value
      sender() ! Done
    case GetTotal => sender() ! CurrentTotal(total)
  }
}

object Total {

  case class Increment(value: Long)

  case object GetTotal

  case class CurrentTotal(value: Long)

}
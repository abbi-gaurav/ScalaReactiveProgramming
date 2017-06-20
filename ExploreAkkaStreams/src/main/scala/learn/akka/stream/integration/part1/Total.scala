package learn.akka.stream.integration.part1

import akka.actor.Actor
import learn.akka.stream.integration.part1.Total._

/**
  * Created by gabbi on 23.04.17.
  */
class Total extends Actor {
  var total: Long = 0

  override def receive: Receive = {
    case _: Init.type => sender() ! Ack
    case Increment(value, _) =>
      total += value
      sender() ! Ack
    case Complete(id) =>
      println(s"Websocket terminated for id: $id")

    case GetTotal => sender() ! CurrentTotal(total)
  }
}

object Total {

  case class Increment(value: Long, lastMessage: String)

  case object Init

  case object Ack

  case class Complete(id: Long)

  case object GetTotal

  case class CurrentTotal(value: Long)

}
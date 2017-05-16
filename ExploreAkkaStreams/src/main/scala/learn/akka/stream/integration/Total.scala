package learn.akka.stream.integration

import akka.actor.Actor
import learn.akka.stream.integration.Total.Increment

/**
  * Created by gabbi on 23.04.17.
  */
class Total extends Actor {
  var total:Long = 0

  override def receive: Receive = {
    case Increment(value) => total += value
  }
}

object Total {

  case class Increment(value: Long)

}
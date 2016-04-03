package w6.failure

import akka.actor.SupervisorStrategy.{Escalate, Restart, Stop}
import akka.actor._

import scala.concurrent.duration._
/**
 * Created by gabbi on 06/09/15.
 */
class Manager extends Actor {
  var restarts = Map.empty[ActorRef, Int].withDefaultValue(0)

  override def receive: Receive = ???

  //val so that startegy is not reinstantiated on each call
  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1.minute) {
    case _: DBException => restarts(sender) match {
      case tooMany if tooMany > 10 =>
        restarts -= sender
        Stop
      case n => restarts =
        restarts.updated(sender, n + 1)
        Restart
    }
    case _: ActorKilledException => Stop
    case _: SerrviceException => Escalate
  }
}

class DBActor extends Actor {
  override def receive: Actor.Receive = ???

}

class ServiceActor extends Actor {
  override def receive: Actor.Receive = ???
}

case class DBException() extends Exception

case class SerrviceException() extends Exception

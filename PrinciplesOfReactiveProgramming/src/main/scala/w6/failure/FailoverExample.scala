package w6.failure

import akka.actor.{ActorRef, Actor, Props, Terminated}

/**
 * Created by gabbi on 20/09/15.
 */
class FailOverManager extends Actor {

  def backup(): Actor.Receive = ???

  def prime(): Receive = {
    val db: ActorRef = context.actorOf(Props[DBActor], "db")
    context.watch(db)

    {
      case Terminated(ref) => context.become(backup())
    }
    
  }

  override def receive: Receive = prime()
}

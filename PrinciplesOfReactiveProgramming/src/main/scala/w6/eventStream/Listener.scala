package w6.eventStream

import akka.actor.Actor
import akka.event.Logging.LogEvent

/**
 * Created by gabbi on 20/09/15.
 */
class Listener extends Actor {
  context.system.eventStream.subscribe(self, classOf[LogEvent])

  override def receive: Receive = {
    case e: LogEvent => ???
  }

  @throws[Exception](classOf[Exception])
  override def postStop(): Unit = context.system.eventStream.unsubscribe(self)
}

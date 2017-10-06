package remoting

import akka.actor._

import scala.concurrent.duration._
import scala.language.postfixOps

class RemoteLookupProxy(path: String) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)

  private def sendIdentifyRequest(): Unit = {
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }


  override def receive: Receive = identify

  private def identify: Receive = {
    case ActorIdentity(`path`, Some(actorRef)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching to active state")
      context.become(active(actorRef))
      context.watch(actorRef)

    case ActorIdentity(`path`, None) =>
      log.error(s"Actors with path $path not available")

    case ReceiveTimeout => sendIdentifyRequest()

    case msg: Any => log.error(s"Ignoring message $msg, actor not active yet")
  }

  private def active(actorRef: ActorRef): Receive = {
    case Terminated(actor) =>
      log.info(s"Remote actor termnated")
      log.info("Switching to identify state")
      context.become(identify)
      sendIdentifyRequest()

    case msg: Any => actorRef forward msg
  }
}

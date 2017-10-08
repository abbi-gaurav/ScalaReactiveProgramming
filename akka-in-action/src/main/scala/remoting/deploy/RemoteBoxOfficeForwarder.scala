package remoting.deploy

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}
import akka.util.Timeout
import api.common.actors.BoxOffice

import scala.concurrent.duration._
import scala.language.postfixOps

object RemoteBoxOfficeForwarder {
  def props(implicit timeout: Timeout) = Props(new RemoteBoxOfficeForwarder())

  def name = "forwarder"
}

class RemoteBoxOfficeForwarder(implicit timeout: Timeout) extends Actor with ActorLogging {
  context.setReceiveTimeout(3 seconds)

  deployAndWatch()

  override def receive: Receive = deploying

  private def deployAndWatch(): Unit = {
    val actor = context.actorOf(BoxOffice.props, BoxOffice.name)
    context.watch(actor)
    log.info("switching to maybe active state")
    context.become(maybeActive(actor))
    context.setReceiveTimeout(Duration.Undefined)
  }

  private def deploying: Receive = {
    case ReceiveTimeout => deployAndWatch()
    case msg: Any =>
      log.info(s"ignoring msg $msg, remote actor not active yet")
  }

  private def maybeActive(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info(s"Actor $actorRef terminated")
      log.info("switching to deploying state")
      context.become(deploying)
      context.setReceiveTimeout(3 seconds)
      deployAndWatch()

    case msg: Any => actor forward msg
  }
}

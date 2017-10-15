package routing.routers

import akka.actor.{Actor, ActorLogging, ActorRef}

class RedirectActor(pipe: ActorRef) extends Actor {
  println("RedirectActor instance created")

  def receive: Receive = {
    case msg: AnyRef =>
      pipe ! msg
  }
}

sealed trait RouteState

case object RouteStateOn extends RouteState

case object RouteStateOff extends RouteState

class SwitchRouter(normalFlow: ActorRef, cleanUp: ActorRef)
  extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: AnyRef => off(msg)
  }

  private def on: Receive = {
    case RouteStateOn => log.warning("received while already in on state")
    case RouteStateOff => context.become(off)
    case msg: AnyRef => normalFlow ! msg
  }

  private def off: Receive = {
    case RouteStateOn => context.become(on)
    case RouteStateOff => log.warning("Received while already in off state")
    case msg: AnyRef => cleanUp ! msg
  }
}

class SwitchRouter2(normalFlow: ActorRef, cleanUp: ActorRef)
  extends Actor with ActorLogging {
  override def receive: Receive = {
    case msg: AnyRef => off(msg)
  }

  private def on: Receive = {
    case RouteStateOn => log.warning("received while already in on state")
    case RouteStateOff => context.unbecome()
    case msg: AnyRef => normalFlow ! msg
  }

  private def off: Receive = {
    case RouteStateOn => context.become(on)
    case RouteStateOff => log.warning("Received while already in off state")
    case msg: AnyRef => cleanUp ! msg
  }
}


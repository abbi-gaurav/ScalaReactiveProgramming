package routing.slip

import akka.actor.ActorRef

case class RouteSlipMessage(routeSlip: Seq[ActorRef], message: AnyRef)

trait RouteSlip {
  def sendMessageToNextTask(routeSlip: Seq[ActorRef], message: AnyRef): Unit = {
    val nextTask = routeSlip.head
    val newSlip = routeSlip.tail

    if (newSlip.isEmpty) {
      nextTask ! message
    } else {
      nextTask ! RouteSlipMessage(
        routeSlip = newSlip,
        message = message
      )
    }
  }
}

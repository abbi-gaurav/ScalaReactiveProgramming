package routing.slip

import akka.actor.Actor

class PaintCar(color: String) extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, car: Car) => sendMessageToNextTask(routeSlip, car.copy(color = color))
  }
}

class AddNavigation() extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, car: Car) => sendMessageToNextTask(routeSlip, car.copy(hasNavigation = true))
  }
}

class AddParkingSensors() extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(routeSlip, car: Car) => sendMessageToNextTask(routeSlip, car.copy(hasParkingSensors = true))
  }
}

package routing.slip

import akka.actor.{Actor, ActorRef, Props}

class SlipRouter(endStep: ActorRef) extends Actor with RouteSlip {
  val paintBlack: ActorRef = context.actorOf(Props(new PaintCar("black")), "paintBlack")
  val paintGray: ActorRef = context.actorOf(Props(new PaintCar("gray")), "paintGray")
  val addNavigation: ActorRef = context.actorOf(Props(new AddNavigation()), "addNavigation")
  val addParkingSensors: ActorRef = context.actorOf(Props(new AddParkingSensors()), "addParkingSensors")

  override def receive: Receive = {
    case Order(options) =>
      val routingSlip: Seq[ActorRef] = createRoutingSlip(options)
      sendMessageToNextTask(routingSlip, Car())

  }

  private def createRoutingSlip(options: Seq[CarOptions.Value]): Seq[ActorRef] = {
    val tasks: Seq[ActorRef] = options.map {
      case CarOptions.COLOR_GRAY => paintGray
      case CarOptions.NAVIGATION => addNavigation
      case CarOptions.PARKING_SENSORS => addParkingSensors
    }

    if (options.contains(CarOptions.COLOR_GRAY)) {
      tasks.:+(endStep)
    } else {
      tasks.+:(paintBlack).:+(endStep)
    }
  }
}

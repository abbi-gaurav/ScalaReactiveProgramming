package routing.routers

import akka.actor.{Actor, ActorRef, Kill, PoisonPill, Props, Terminated}

class GetLicenseCreator(numberOfActors: Int, nextStep: ActorRef) extends Actor {
  private var createdActors = Seq[ActorRef]()

  override def preStart(): Unit = {
    super.preStart()
    createdActors = (0 until numberOfActors) map { idx =>
      context.actorOf(Props(new GetLicense(nextStep)), s"GetLicense$idx")
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println(s"restart ${self.path.toString}")
    super.preRestart(reason, message)
  }

  override def receive: Receive = {
    case "KillFirst" =>
      createdActors.headOption.foreach(_ ! Kill)
      createdActors = createdActors.tail
    case _ => throw new IllegalArgumentException("not supported")
  }
}

class GetLicenseCreator2(numberOfActors: Int, nextStep: ActorRef) extends Actor {
  private var createdActors = Seq[ActorRef]()

  override def preStart(): Unit = {
    super.preStart()
    createdActors = (0 until numberOfActors) map { idx =>
      val child = context.actorOf(Props(new GetLicense(nextStep)), s"GetLicense$idx")
      context.watch(child)
      child
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println(s"restart ${self.path.toString}")
    super.preRestart(reason, message)
  }

  override def receive: Receive = {
    case "KillFirst" => context.children.headOption.foreach(_ ! PoisonPill)

    case Terminated(deadChild) =>
      val newChild = context.actorOf(Props(new GetLicense(nextStep)), deadChild.path.name)
      context.watch(newChild)
  }

}

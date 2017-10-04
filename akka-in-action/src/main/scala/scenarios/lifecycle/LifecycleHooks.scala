package scenarios.lifecycle

import akka.actor.{Actor, ActorLogging}

class LifecycleHooks extends Actor with ActorLogging {
  println("constructor")



  override def preStart(): Unit = println("preStart")


  override def postStop(): Unit = println("postStop")

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    println("preRestart")
    super.preRestart(reason, message)
  }

  override def postRestart(reason: Throwable): Unit = {
    println("postRestart")
    super.postRestart(reason)
  }

  override def receive: Receive = {
    case "restart" => throw new IllegalArgumentException("force restart")
    case msg: AnyRef =>
      println("receive")
      sender() ! msg
  }

}

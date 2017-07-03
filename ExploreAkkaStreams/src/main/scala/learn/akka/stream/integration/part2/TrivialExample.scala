package learn.akka.stream.integration.part2

import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

/**
  * Created by gabbi on 20.06.17.
  */
class PrintSomeNumbers(implicit materializer: ActorMaterializer) extends Actor {
  private implicit val executionContext = context.system.dispatcher

  Source(1 to 10)
    .map(_.toString)
    .runForeach(println)
    .map(_ => self ! "done")

  override def receive: Receive = {
    case "done" =>
      println("all done")
      context.stop(self)
  }
}

object TrivialExample extends App {
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()

  actorSystem.actorOf(Props(classOf[PrintSomeNumbers], materializer))
}

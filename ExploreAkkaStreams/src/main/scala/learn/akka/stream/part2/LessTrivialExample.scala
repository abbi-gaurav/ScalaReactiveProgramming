package learn.akka.stream.part2

import akka.Done
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.{ActorMaterializer, KillSwitches, UniqueKillSwitch}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Created by gabbi on 20.06.17.
  */
object LessTrivialExample extends App {
  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  private val actorRef = system.actorOf(Props(classOf[PrintMoreNumbers], materializer))

  system.scheduler.scheduleOnce(5 seconds) {
    actorRef ! "stop"
  }
}

class PrintMoreNumbers(implicit actorMaterializer: ActorMaterializer) extends Actor {
  private implicit val executionContext = context.system.dispatcher

  val (killSwitch: UniqueKillSwitch, done: Future[Done]) = Source.tick(0 seconds, 1 second, 1)
    .scan(0)(_ + _)
    .map(_.toString)
    .viaMat(KillSwitches.single)(Keep.right)
    .toMat(Sink.foreach(println))(Keep.both)
    .run()

  done.map(_ => self ! "done")

  override def receive: Receive = {
    case "stop" =>
      println("Stopping")
      killSwitch.shutdown()
    case "done" =>
      println("done")
      context.stop(self)
  }
}

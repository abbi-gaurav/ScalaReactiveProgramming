package org.explore

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props, _}
import akka.pattern._
import akka.util.Timeout
import org.explore.ActorA.{Response, SampleMsg}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by gabbi on 04/12/2016.
  */
object AkkaBasic extends App {
  val system: ActorSystem = ActorSystem("AkkaBasic")
  val actor: ActorRef = system.actorOf(Props[ActorA])

  import system.dispatcher

  private implicit val timeout = Timeout(5 seconds)

  actor ? SampleMsg("s") onComplete {
    case Success(x) => println(x)
    case Failure(f) => f.printStackTrace()
  }

  System.in.read()
  val terminated = Await.result(system.terminate(), 2 seconds)
  println(terminated)
}

class ActorA extends Actor with ActorLogging {
  override def receive: Receive = {
    case SampleMsg(messageString) => sender() ! Response(messageString.toUpperCase)

  }
}

object ActorA {

  case class SampleMsg(messageString: String)

  case class Response(string: String)

}

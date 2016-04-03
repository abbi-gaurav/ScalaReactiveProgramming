package w5.urlFetcher

import akka.actor.{ReceiveTimeout, Props, Actor}
import akka.actor.Actor.Receive
import w5.urlFetcher.Receptionist.Failed
import scala.concurrent.duration._
/**
 * Created by gabbi on 07/06/15.
 */
class Main extends Actor{
  val receptionist = context.actorOf(Props[Receptionist], "receptionist")
  context.watch(receptionist)

  receptionist ! Receptionist.Get("http://www.google.com")
  receptionist ! Receptionist.Get("http://www.google.com/1")
  receptionist ! Receptionist.Get("http://www.google.com/2")
  receptionist ! Receptionist.Get("http://www.google.com/3")
  receptionist ! Receptionist.Get("http://www.google.com/4")

  context.setReceiveTimeout(10 seconds)
  override def receive: Receive = {

    case Receptionist.Result(url, links) =>
      println(links.toVector.sorted.mkString(start = s"Results for $url:\n",sep="\n", end="\n"))

    case Failed(url) =>
      println(s"failed to fetch details for $url")

    case ReceiveTimeout => context.stop(self)
  }

  override def postStop():Unit = {
    AsyncWebClient.shutdown()
  }
}

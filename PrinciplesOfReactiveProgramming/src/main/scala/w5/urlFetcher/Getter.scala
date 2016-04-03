package w5.urlFetcher

import akka.actor.{Status, Actor}
import akka.actor.Status.Status
import akka.pattern.pipe
import w5.urlFetcher.Getter.Abort

/**
 * Created by gabbi on 07/06/15.
 */
class Getter(url: String, depth: Int) extends Actor {
  def client:WebClient = AsyncWebClient

  private implicit val executor = context.dispatcher
//  private val future = client.get(url)
  //1
  //  future onComplete{
  //    case Success(body) => self ! body
  //    case Failure(err) => self ! Status.Failure(err)
  //  }

  //2
  //  future pipeTo self

  //3
  client get url pipeTo self

  override def receive: Receive = {
    case body: String =>
      for (link <- Utils.findLinks(body, url)) {
        context.parent ! Controller.Check(link, depth)
      }
      stop()

    case _:Status.Failure => stop()

    case Abort => stop()
  }

  def stop2(): Unit = {
    context.parent ! Getter.Done
    context.stop(self)
  }

  def stop(): Unit = {
    context.stop(self)
  }
}

object Getter {
  case object Done

  case object Abort
}

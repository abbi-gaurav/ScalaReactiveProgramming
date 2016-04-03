package w5.urlFetcher

import akka.actor._
import akka.util.Timeout
import w5.urlFetcher.Controller.Check

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

/**
 * Created by gabbi on 07/06/15.
 */

class Controller extends Actor with ActorLogging {
  private var cache: Set[String] = Set.empty[String]
  private implicit val ec: ExecutionContextExecutor = context.dispatcher

  context.system.scheduler.scheduleOnce(10 seconds, self, Timeout)

  context.setReceiveTimeout(10 seconds)

  override val supervisorStrategy: SupervisorStrategy = OneForOneStrategy(maxNrOfRetries = 5) {
    case _: Exception => SupervisorStrategy.Restart
  }

  override def receive: Receive = {
    case Check(url, depth) =>
      log.debug("{} checking {}", depth, url)
      if (!cache(url) && depth > 0) {
        context.watch(context.actorOf(Props(new Getter(url, depth - 1))))
      }
      cache += url

    case Terminated(_) =>
      if (context.children.isEmpty) context.parent ! Controller.Result(cache)

    case ReceiveTimeout => context.children foreach context.stop

    case Timeout => context.children foreach context.stop
  }
}

object Controller {

  case class Result(links: Set[String])

  case class Check(link: String, depth: Int)

}

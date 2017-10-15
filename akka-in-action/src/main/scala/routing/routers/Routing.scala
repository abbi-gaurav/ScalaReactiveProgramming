package routing.routers

import akka.actor.{Actor, ActorRef, ActorSystem, OneForOneStrategy, Props, SupervisorStrategy}
import akka.dispatch.Dispatchers
import akka.routing._
import structure.{ImageProcessing, Photo}

import scala.collection.immutable
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

case class PerformanceRoutingMessage(photo: String,
                                     license: Option[String],
                                     processedBy: Option[String]
                                    )

case class SetService(id: String, serviceTime: FiniteDuration)

class GetLicense(pipe: ActorRef, initialServiceTime: FiniteDuration = 0 millis) extends Actor {
  private var id: String = self.path.name
  private var serviceTime = initialServiceTime

  override def receive: Receive = {
    case SetService(str, finiteDuration) =>
      this.id = str
      this.serviceTime = finiteDuration
      Thread.sleep(100)
    case msg@PerformanceRoutingMessage(photo, _, _) =>
      Thread.sleep(serviceTime.toMillis)
      pipe ! msg.copy(
        license = ImageProcessing.getLicense(photo),
        processedBy = Some(id)
      )
  }
}

class SpeedRoutingLogic(minSpeed: Int, normalFlowPath: String, cleanUpPath: String) extends RoutingLogic {
  override def select(message: Any, routees: immutable.IndexedSeq[Routee]): Routee = message match {
    case Photo(_, speed) => if (speed > minSpeed) findRoutee(routees, normalFlowPath) else findRoutee(routees, cleanUpPath)
  }

  private def findRoutee(routees: IndexedSeq[Routee], path: String): Routee = {
    val routeeList: IndexedSeq[Routee] = routees.flatMap {
      case actorRefRoutee: ActorRefRoutee => Seq(actorRefRoutee)
      case SeveralRoutees(routeeSeq) => routeeSeq
    }

    routeeList
      .find { case routee: ActorRefRoutee => routee.ref.path.toString.endsWith(path) }
      .getOrElse(NoRoutee)
  }
}

case class SpeedRouterPool(minSpeed: Int, normalFlow: Props, cleanUpFlow: Props) extends Pool {
  override def nrOfInstances(sys: ActorSystem): Int = 1

  override def resizer: Option[Resizer] = None

  override def supervisorStrategy: SupervisorStrategy = OneForOneStrategy()(SupervisorStrategy.defaultDecider)

  override def createRouter(system: ActorSystem): Router = {
    new Router(new SpeedRoutingLogic(minSpeed, "nomalFlow", "cleanup"))
  }

  override def routerDispatcher: String = Dispatchers.DefaultDispatcherId

}


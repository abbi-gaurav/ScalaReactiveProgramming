package learn.akka.cluster.sample

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSelection, RootActorPath, Terminated}
import akka.cluster.ClusterEvent.MemberUp
import akka.cluster.{Cluster, Member}
import learn.akka.cluster.sample.ProducerActor.{SelectionFailed, SelectionStatus, SelectionSucceeded}

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class ProducerActor extends Actor with ActorLogging {

  import context.dispatcher

  private val cluster: Cluster = Cluster(context.system)
  private var consumers: List[ActorRef] = List.empty[ActorRef]
  private var counter: Long = 0L

  context.system.scheduler.schedule(10.seconds, 2.seconds, self, SimpleMessage)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  override def postStop(): Unit = cluster.unsubscribe(self)

  override def receive: Receive = messenger orElse watcher

  private def messenger: Receive = {
    case SimpleMessage =>
      counter += 1
      val string: String = s"counter-$counter"
      consumers.foreach(_ ! string)
  }

  private def watcher: Receive = {
    case MemberUp(member) => register(member)
    case Terminated(actorRef) => consumers = consumers.filterNot(_ == actorRef)
    case SelectionSucceeded(actorRef) => consumers = actorRef :: consumers
    case SelectionFailed(throwable) => log.error(throwable, "not able to find actor with selection")
  }

  def register(member: Member): Unit = {
    log.info(s"Got member up event -- $member role ${member.roles}")
    val consumerRoleString = Consumer.toString.toLowerCase

    if (member.hasRole(consumerRoleString)) {
      val selection: ActorSelection = context.actorSelection(RootActorPath(member.address) / "user" / consumerRoleString)
      triggerSelection(selection)
    }
  }

  private def triggerSelection(selection: ActorSelection): Unit = {
    val future: Future[SelectionStatus] = (selection.resolveOne(5.seconds) map SelectionSucceeded.apply).recover {
      case throwable: Throwable => SelectionFailed(throwable)
    }

    import akka.pattern.pipe
    pipe(future) to self
  }
}

object ProducerActor {

  sealed trait SelectionStatus

  case class SelectionSucceeded(actorRef: ActorRef) extends SelectionStatus

  case class SelectionFailed(throwable: Throwable) extends SelectionStatus

}

package kvstore

import akka.actor.{Actor, ActorRef, Props}

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

object Replicator {

  case class Replicate(key: String, valueOption: Option[String], id: Long)

  case class Replicated(key: String, id: Long)

  case class Snapshot(key: String, valueOption: Option[String], seq: Long)

  case class SnapshotAck(key: String, seq: Long)

  case object RetryUnacknowledged

  def props(replica: ActorRef): Props = Props(new Replicator(replica))
}

class Replicator(val replica: ActorRef) extends Actor {

  import Replicator._

  import scala.concurrent.duration._

  private implicit val ec: ExecutionContextExecutor = context.dispatcher

  context.system.scheduler.schedule(200 milliseconds, 200 milliseconds, self, RetryUnacknowledged)

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  // map from sequence number to pair of sender and request
  var acks: Map[Long, (ActorRef, Replicate)] = Map.empty[Long, (ActorRef, Replicate)]

  // a sequence of not-yet-sent snapshots (you can disregard this if not implementing batching)
  var pending: Vector[Snapshot] = Vector.empty[Snapshot]

  var _seqCounter: Long = 0L

  def nextSeq: Long = {
    val ret = _seqCounter
    _seqCounter += 1
    ret
  }


  /* TODO Behavior for the Replicator. */
  def receive: Receive = {
    case replicate@Replicate(key, valueOption, id) =>
      val seqId: Long = nextSeq
      replica ! Snapshot(key, valueOption, seqId)
      acks = acks.updated(seqId, (sender(), replicate))
    case SnapshotAck(key, seq) => acks.get(seq).foreach {
      case (sender, replicate) =>
        sender ! Replicated(replicate.key, replicate.id)
        acks = acks - seq
    }
    case RetryUnacknowledged => acks.foreach {
      case (seq, (sender, replicate)) => replica ! Snapshot(replicate.key, replicate.valueOption, seq)
    }
  }

}

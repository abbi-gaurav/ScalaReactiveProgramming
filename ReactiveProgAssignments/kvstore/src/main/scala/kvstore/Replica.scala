package kvstore

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import kvstore.Arbiter._
import kvstore.Persistence.{Persist, Persisted}
import kvstore.Replicator.{Replicate, Replicated, Snapshot, SnapshotAck}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.language.postfixOps

object Replica {

  sealed trait Operation {
    def key: String

    def id: Long
  }

  case class Insert(key: String, value: String, id: Long) extends Operation

  case class Remove(key: String, id: Long) extends Operation

  case class Get(key: String, id: Long) extends Operation

  sealed trait OperationReply

  case class OperationAck(id: Long) extends OperationReply

  case class OperationFailed(id: Long) extends OperationReply

  case class GetResult(key: String, valueOption: Option[String], id: Long) extends OperationReply

  case object RetryUnPersisted

  case class CheckIfDoneAfter1s(id: Long, key: String)

  def props(arbiter: ActorRef, persistenceProps: Props): Props = Props(new Replica(arbiter, persistenceProps))
}

class Replica(val arbiter: ActorRef, persistenceProps: Props) extends Actor {

  import Replica._

  private var expectedSeq: Long = 0L

  /*
   * The contents of this actor is just a suggestion, you can implement it in any way you like.
   */

  var kv: Map[String, String] = Map.empty[String, String]
  // a map from secondary replicas to replicators
  var secondaries: Map[ActorRef, ActorRef] = Map.empty[ActorRef, ActorRef]
  // the current set of replicators
  var replicators: Set[ActorRef] = Set.empty[ActorRef]

  val persistor: ActorRef = context.actorOf(persistenceProps)

  //map ((id,key) -> (Set[Replicators], Option[Sender]))
  var leaderAcknowledgementWaiters = Map.empty[(Long, String), (Set[ActorRef], Option[ActorRef])]

  var persistenceToBeAcknowledged = Map.empty[Long, (ActorRef, Persist)]

  private implicit val ec: ExecutionContextExecutor = context.dispatcher
  context.system.scheduler.schedule(100 milliseconds, 100 milliseconds, self, RetryUnPersisted)

  arbiter ! Join

  def receive = {
    case JoinedPrimary => context.become(leader)
    case JoinedSecondary => context.become(replica)
  }

  /* TODO Behavior for  the leader role. */
  val leader: Receive = {
    case Insert(key, value, id) => primayOnKeyAddOrRemove(key, id, Some(value))
    case Remove(key, id) => primayOnKeyAddOrRemove(key, id, None)
    case Get(key, id) => sender ! GetResult(key, kv.get(key), id)
    case Persisted(key, id) => handlePersistedMessage(key, id) foreach { case (clientRef, Persist(keyInReply, _, _)) => ack4rmLeaderIfPossible(id, clientRef, keyInReply) }
    case RetryUnPersisted => handleRetryPersist()
    case Replicas(currentReplicas) => handleCurrentReplicas(currentReplicasParam = currentReplicas)
    case Replicated(key, id) =>
      val exitingOpt: Option[(Set[ActorRef], Option[ActorRef])] = leaderAcknowledgementWaiters.get((id, key))
      exitingOpt.foreach { case (waitingOnReplicas, orgSenderOpt) =>
        leaderAcknowledgementWaiters = leaderAcknowledgementWaiters.updated((id, key), (waitingOnReplicas - sender(), orgSenderOpt))
        //filter all for which no waiting replicas are left
        leaderAcknowledgementWaiters = leaderAcknowledgementWaiters.filter { case (_, value) => value._1.nonEmpty }

        orgSenderOpt.foreach(ack4rmLeaderIfPossible(id, _, key))
      }
    case CheckIfDoneAfter1s(id, key) => {
      persistenceToBeAcknowledged.get(id).map(_._1).orElse(leaderAcknowledgementWaiters.get((id, key)).flatMap(_._2)).foreach { clientRef =>
        persistenceToBeAcknowledged = persistenceToBeAcknowledged - id
        leaderAcknowledgementWaiters = leaderAcknowledgementWaiters - ((id, key))
        clientRef ! OperationFailed(id)
      }
    }
  }

  private def primayOnKeyAddOrRemove(key: String, id: Long, valueOption: Option[String]): Unit = {
    workOnPersisterUpdates(key = key, valueOpt = valueOption, id, sender())
    propagate2Replicas(key, id, valueOption)
  }

  private def propagate2Replicas(key: String, id: Long, valueOption2Replicate: Option[String]): Unit = {
    replicators.foreach { repl =>
      repl ! Replicate(key, valueOption2Replicate, id)
      val existingOpt = leaderAcknowledgementWaiters.getOrElse((id, key), (Set[ActorRef](), Some(sender)))
      leaderAcknowledgementWaiters = leaderAcknowledgementWaiters.updated((id, key), (existingOpt._1 + repl, existingOpt._2))
    }
    context.system.scheduler.scheduleOnce(1 second, self, CheckIfDoneAfter1s(id, key))
  }

  private def handleCurrentReplicas(currentReplicasParam: Set[ActorRef]): Unit = {
    val currentReplicas = currentReplicasParam - self
    //get newly added
    val inHouseReplicaSet: Set[ActorRef] = secondaries.keySet
    //get deleted
    val deleted: Set[ActorRef] = inHouseReplicaSet.diff(currentReplicas)
    work4Deleted(deleted)
    val newlyAdded: Set[ActorRef] = currentReplicas.diff(inHouseReplicaSet)
    work4Added(added = newlyAdded)
  }

  private def work4Deleted(deleted: Set[ActorRef]): Unit = {
    deleted.foreach { deletedReplica =>
      secondaries.get(deletedReplica).foreach { toBedeletedReplicator =>
        toBedeletedReplicator ! PoisonPill
        replicators = replicators - toBedeletedReplicator
        leaderAcknowledgementWaiters = leaderAcknowledgementWaiters.map { case (tuple, (replicatorsRefs, clientRefOpt)) => (tuple, (replicatorsRefs - toBedeletedReplicator, clientRefOpt)) }
        val (newV, possibleAcs) = leaderAcknowledgementWaiters.span { case (_, value) => value._1.nonEmpty }
        leaderAcknowledgementWaiters = newV
        possibleAcs.foreach { case ((id, key), (set, mayBeClient)) =>
          mayBeClient.foreach(ack4rmLeaderIfPossible(id, _, key))
        }
      }
      secondaries = secondaries - deletedReplica
    }
  }

  private def work4Added(added: Set[ActorRef]): Unit = {
    added.foreach { addedReplica =>
      val addedReplicator: ActorRef = context.actorOf(Replicator.props(addedReplica))
      secondaries = secondaries.updated(addedReplica, addedReplicator)
      replicators = replicators + addedReplicator
      Stream.from(0).zip(kv).foreach { case (id, (key, value)) =>
        addedReplicator ! Replicate(key, Some(value), id)
      }
    }
  }

  private def invokePersister(key: String, valueOpt: Option[String], id: Long, ref2SendAck: ActorRef): Unit = {
    val persistMsg: Persist = Persist(key, valueOpt, id)
    persistor ! persistMsg
    persistenceToBeAcknowledged = persistenceToBeAcknowledged.updated(id, (ref2SendAck, persistMsg))
  }

  private def ack4rmLeaderIfPossible(id: Long, clientRef: ActorRef, key: String) = {
    if (!(leaderAcknowledgementWaiters contains(id, key)) && !(persistenceToBeAcknowledged contains id)) {
      clientRef ! OperationAck(id)
    }
  }

  /* TODO Behavior for the replica role. */
  val replica: Receive = {
    case Get(key, id) =>
      sender ! GetResult(key, kv.get(key), id)
    case Snapshot(key, valueOpt, seq) =>
      if (seq > expectedSeq) {}
      else {
        if (expectedSeq == seq) {
          //do main work here
          workOnPersisterUpdates(key, valueOpt, seq, sender())
        } else {
          updateSeq(seq)
          sender ! SnapshotAck(key, seq)
        }
      }
    case Persisted(key, seq) =>
      updateSeq(seq)
      handlePersistedMessage(key, seq).foreach { case (clientRef, _) => clientRef ! SnapshotAck(key, seq) }

    case RetryUnPersisted => handleRetryPersist()
  }

  private def handleRetryPersist(): Unit = {
    persistenceToBeAcknowledged foreach {
      case (id, (sender, persistMsg)) => persistor ! persistMsg
    }
  }

  private def handlePersistedMessage(key: String, id: Long): Option[(ActorRef, Persist)] = {
    val ret = persistenceToBeAcknowledged.get(id)
    persistenceToBeAcknowledged = persistenceToBeAcknowledged - id
    ret
  }

  private def updateSeq(seq: Long): Unit = {
    expectedSeq = if (seq + 1 > expectedSeq) seq + 1 else expectedSeq
  }

  private def workOnPersisterUpdates(key: String, valueOpt: Option[String], seq: Long, ackRef: ActorRef): Unit = {
    valueOpt match {
      case Some(value) => kv = kv.updated(key, value)
      case None => kv = kv - key
    }
    invokePersister(key, valueOpt, seq, ackRef)
  }

}


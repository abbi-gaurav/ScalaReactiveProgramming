/**
 * Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com>
 */
package actorbintree

import actorbintree.BinaryTreeNode.{CopyFinished, CopyTo}
import actorbintree.BinaryTreeSet._
import akka.actor._

import scala.collection.immutable.Queue

object BinaryTreeSet {

  trait Operation {
    def requester: ActorRef

    def id: Int

    def elem: Int
  }

  trait OperationReply {
    def id: Int
  }

  /** Request with identifier `id` to insert an element `elem` into the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Insert(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to check whether an element `elem` is present
    * in the tree. The actor at reference `requester` should be notified when
    * this operation is completed.
    */
  case class Contains(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request with identifier `id` to remove the element `elem` from the tree.
    * The actor at reference `requester` should be notified when this operation
    * is completed.
    */
  case class Remove(requester: ActorRef, id: Int, elem: Int) extends Operation

  /** Request to perform garbage collection */
  case object GC

  /** Holds the answer to the Contains request with identifier `id`.
    * `result` is true if and only if the element is present in the tree.
    */
  case class ContainsResult(id: Int, result: Boolean) extends OperationReply

  /** Message to signal successful completion of an insert or remove operation. */
  case class OperationFinished(id: Int) extends OperationReply

}


class BinaryTreeSet extends Actor {

  import BinaryTreeSet._

  def createRoot: ActorRef = context.actorOf(BinaryTreeNode.props(0, initiallyRemoved = true))

  var root = createRoot

  // optional
  var pendingQueue = Queue.empty[Operation]

  // optional
  def receive = normal

  // optional
  /** Accepts `Operation` and `GC` messages. */
  val normal: Receive = {
    case operation: Operation => root ! operation
    case GC => {
      val newRoot: ActorRef = createRoot
      root ! CopyTo(newRoot)
      context.become(garbageCollecting(newRoot))
    }
  }

  // optional
  /** Handles messages while garbage collection is performed.
    * `newRoot` is the root of the new binary tree where we want to copy
    * all non-removed elements into.
    */
  def garbageCollecting(newRoot: ActorRef): Receive = {
    case operation: Operation => pendingQueue = pendingQueue enqueue operation
    case GC => //ignore another GC while one is on progress
    case CopyFinished => {
      root ! PoisonPill
      root = newRoot
      invokeAllPending
      context.become(normal)
    }
  }

  private def invokeAllPending = {
    pendingQueue foreach (root ! _)
    pendingQueue = Queue.empty[Operation]
  }
}

object BinaryTreeNode {

  trait Position

  case object Left extends Position

  case object Right extends Position

  case class CopyTo(treeNode: ActorRef)

  case object CopyFinished

  def props(elem: Int, initiallyRemoved: Boolean) = Props(classOf[BinaryTreeNode], elem, initiallyRemoved)
}

class BinaryTreeNode(val elem: Int, initiallyRemoved: Boolean) extends Actor {

  import BinaryTreeNode._

  var subtrees = Map[Position, ActorRef]()
  var removed = initiallyRemoved

  // optional
  def receive = normal

  private def containInPosition(contains: Contains, position: Position): Unit = subtrees get position match {
    case Some(subNode) => subNode ! contains
    case _ => contains.requester ! ContainsResult(contains.id, false)
  }

  private def insertInPosition(insert: Insert, position: Position): Unit = subtrees.get(position) match {
    case Some(subNode) => subNode ! insert
    case _ =>
      subtrees = subtrees updated(position, context.actorOf(props(insert.elem, false)))
      insert.requester ! OperationFinished(insert.id)
  }

  private def getPosition(operationElement: Int): Option[Position] = if (operationElement < elem) Some(Left) else if (operationElement > elem) Some(Right) else None

  private def removeInPosition(remove: Remove, position: Position): Unit = subtrees.get(position) match {
    case Some(subNode) => subNode ! remove
    case _ => remove.requester ! OperationFinished(remove.id)
  }

  private def handle[T <: Operation](operation: T)(onEquals: Int => OperationReply)(forChild: (T, Position) => Unit) = getPosition(operation.elem) match {
    case Some(position) => forChild(operation, position)
    case _ => operation.requester ! onEquals(operation.id)
  }

  /** Handles `Operation` messages and `CopyTo` requests. */
  val normal: Receive = {
    case insert@Insert(_, _, _) => handle(insert) { id =>
      removed = false
      OperationFinished(id)
    }(insertInPosition)
    case contains@Contains(_, _, _) =>
      handle(contains) { id =>
        ContainsResult(id, !removed)
      }(containInPosition)
    case remove@Remove(_, _, _) =>
      handle(remove) { id =>
        removed = true
        OperationFinished(id)
      }(removeInPosition)
    case CopyTo(treeNode) => {
      val childActorRefs: Set[ActorRef] = subtrees.values.toSet
      if (removed && childActorRefs.isEmpty) {
        sender ! CopyFinished
      } else {
        for {child <- childActorRefs} {
          child ! CopyTo(treeNode)
        }
        if (!removed) {
          treeNode ! Insert(self, -1, elem = elem)
          context.become(copying(childActorRefs, false))
        } else {
          //this is just to check
          context.become(copying(childActorRefs, true))
        }
      }
    }
  }

  // optional
  /** `expected` is the set of ActorRefs whose replies we are waiting for,
    * `insertConfirmed` tracks whether the copy of this node to the new tree has been confirmed.
    */
  def copying(expected: Set[ActorRef], insertConfirmed: Boolean): Receive = {
    case OperationFinished(-1) => if (expected.isEmpty) signalCopyFinish else context.become(copying(expected, true))
    case CopyFinished => {
      val currentExpected = expected - sender
      if (insertConfirmed && currentExpected.isEmpty) {
        signalCopyFinish
      } else {
        context.become(copying(currentExpected, insertConfirmed))
      }
    }
  }


  private def signalCopyFinish: Unit = {
    context.parent ! CopyFinished
    context.become(normal)
  }
}

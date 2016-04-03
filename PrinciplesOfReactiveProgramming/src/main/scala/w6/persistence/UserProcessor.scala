package w6.persistence

import akka.actor.ActorPath
import akka.persistence.{AtLeastOnceDelivery, PersistentActor}

/**
 * Created by gabbi on 04/10/15.
 */
class UserProcessor(userId: String) extends PersistentActor {
  var state: State = State(Vector.empty, disabled = false)

  override def receiveRecover: Receive = {
    case e: Event => updateState(e)
  }

  override def receiveCommand: Receive = {
    case NewPost(text, id) =>
      if (state.disabled) sender() ! BlogNotPosted(id, "quota reached")
      else {
        persist(PostCreated(text = text)) { event =>
          updateState(event)
          sender() ! BlogPosted(id)
        }
        persist(QuotaReached)(updateState)
      }
  }

  private def updateState(e: Event): Unit = state = state.updated(e)

  override def persistenceId: String = userId
}

class UserProcessor2(userId: String, publisher: ActorPath) extends PersistentActor with AtLeastOnceDelivery {
  var state: State = State(Vector.empty, disabled = false)

  override def receiveRecover: Receive = {
    case e@PostCreated(text) =>
      deliver(publisher)(PublishPost(text,_))
      updateState(e)
    case e: Event => updateState(e)
    case PostPublished(id) => confirmDelivery(id)
  }

  override def receiveCommand: Receive = {
    case NewPost(text, id) =>
      if (!state.disabled) {
        persist(PostCreated(text)) { e =>
          deliver(publisher)(PublishPost(text, _))
          updateState(e)
          sender() ! BlogPosted(id)
        }
      } else {
        sender() ! BlogNotPosted(id, "quota reached")
      }
    case PostPublished(id) => confirmDelivery(id)
  }

  override def persistenceId: String = userId

  private def updateState(e: Event): Unit = state = state.updated(e)
}

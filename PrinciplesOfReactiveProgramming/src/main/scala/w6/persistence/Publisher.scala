package w6.persistence

import akka.persistence.PersistentActor

/**
 * Created by gabbi on 04/10/15.
 */
class Publisher extends PersistentActor{
  private var expectedId:Long = 0L

  override def receiveRecover: Receive = {
    case PostPublished(id) => expectedId = id + 1
  }

  override def receiveCommand: Receive = {
    case PublishPost(text, id) =>
      if(id > expectedId) ()
      else if(id < expectedId) sender() ! PostPublished(id)
      else persist(PostPublished(id)){e =>
        sender() ! e
        //modify view model
        expectedId += 1
      }
  }

  override def persistenceId: String = "publisher"
}

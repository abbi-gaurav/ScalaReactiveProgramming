package w2.observer

/**
 * Created by gabbi on 07/05/15.
 */
trait Publisher {
  private var subscribers: Set[Subscriber] = Set()

  def subscribe(subscriber: Subscriber): Unit = subscribers += subscriber

  def publish(): Unit = for {subscriber <- subscribers} subscriber.handle(this)
}

trait Subscriber {
  def handle(p: Publisher): Unit
}

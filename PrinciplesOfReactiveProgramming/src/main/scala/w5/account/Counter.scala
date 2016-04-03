package w5.account

import akka.actor.{Actor, Props}

/**
 * Created by gabbi on 06/06/15.
 */
class Counter extends Actor {
  var count: Int = 0

  override def receive: Receive = {
    case "incr" => count += 1
    case "get" => sender ! count
  }
}

class CounterBecome extends Actor {
  def count(n: Int): Receive = PartialFunction {
    case "incr" => context.become(count(n + 1))
    case "get" => sender ! n
  }

  override def receive: Receive = count(0)
}

class Main extends Actor{
  private val counter = context.actorOf(Props[CounterBecome], "counter")
  counter ! "incr"
  counter ! "incr"
  counter ! "incr"
  counter ! "get"
  override def receive: Actor.Receive = {
    case count:Int =>
      println(s"count is $count")
      context.stop(self)
  }
}
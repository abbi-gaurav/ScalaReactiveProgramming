package w4.observables

import rx.lang.scala.Subscription
import rx.lang.scala.subscriptions.{MultipleAssignmentSubscription, CompositeSubscription}

/**
 * Created by gabbi on 05.06.15.
 */
object HelloSubscriptions extends App {

  def composite(): Unit = {
    val a = Subscription {
      println("a")
    }
    val b = Subscription {
      println("b")
    }

    val s = CompositeSubscription(a, b)
    println(a isUnsubscribed)
    println(s isUnsubscribed)
    s unsubscribe()
    println(s isUnsubscribed)
    println(a isUnsubscribed)

    val c = Subscription {
      println("c")
    }

    val n = s += c
    println(n isUnsubscribed)
    println(c isUnsubscribed)
  }


  def simple(): Unit = {
    val s: Subscription = Subscription {
      println("bye bye black bird!!!")
    }

    s.unsubscribe()
    s.unsubscribe()
  }

  def multiAssignment(): Unit = {
    val s = MultipleAssignmentSubscription()
    val a = Subscription {
      println("a")
    }
    val b = Subscription {
      println("b")
    }
    s.subscription = a
    s.subscription = b
    s.unsubscribe()
    s.subscription = Subscription {
      println("c")
    }
  }


  //  simple()
//  composite()
  multiAssignment()
  readLine()

}

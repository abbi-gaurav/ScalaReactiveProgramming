package w4.observables

import rx.lang.scala.Observable

import scala.concurrent.duration._

/**
 * Created by gabbi on 31/05/15.
 */
object HelloObservables extends App{
  val ticks: Observable[Long] = Observable.interval(1 seconds)
  val evens = ticks filter (_ % 2 == 0)
  val bufs: Observable[Seq[Long]] = evens.slidingBuffer(count = 2, skip = 1)
  val s = bufs subscribe {
    println(_)
  }

  System.in.read()

  s unsubscribe()
}

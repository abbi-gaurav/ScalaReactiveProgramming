package w4.observables

import rx.lang.scala.Observable
import scala.concurrent.duration._
/**
 * Created by gabbi on 03.06.15.
 */
object FlattNestedStreams extends App{
  val xs:Observable[Int] = Observable.from(List(3,2,1))
  val yss:Observable[Observable[Int]] = xs.map(x => Observable.interval(x seconds).map(_ => x).take(2))
  val zs:Observable[Int] = yss flatten

  val s = zs subscribe{println(_)}

  readLine()

  s unsubscribe()

}

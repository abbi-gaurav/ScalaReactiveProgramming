package w4.observables

import rx.lang.scala.{Subscription, Subject}
import rx.lang.scala.subjects.{BehaviorSubject, AsyncSubject, ReplaySubject, PublishSubject}

/**
 * Created by gabbi on 05.06.15.
 */
object HelloSubjects extends App{

  def testSubject(subject: Subject[Int]  ): Unit = {
    val a: Subscription = subject.subscribe { x => println(s"a ${x}") }

    val b: Subscription = subject.subscribe { x => println(s"b ${x}") }

    subject.onNext(42)
    a.unsubscribe()
    subject.onNext(71)
    subject.onCompleted()

    val c: Subscription = subject.subscribe(x => println(s"c $x"))
    subject onNext 31
  }

//  testSubject(PublishSubject[Int]())
//  testSubject(ReplaySubject[Int]())
//  testSubject(AsyncSubject[Int]())
  testSubject(BehaviorSubject[Int]())
  readLine()
}

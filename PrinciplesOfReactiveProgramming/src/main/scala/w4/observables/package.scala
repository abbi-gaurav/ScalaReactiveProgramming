package w4

import rx.lang.scala.Observable
import rx.lang.scala.subjects.AsyncSubject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import scala.util.control.Breaks._
/**
 * Created by gabbi on 05.06.15.
 */
package object observables {
  def fromFuture[T](future: Future[T])(implicit executionContext: ExecutionContext):Observable[T] = {
    val subject = AsyncSubject[T]()
    future onComplete{
      case Failure(throwable) =>subject.onError(throwable)
      case Success(value) =>
        subject.onNext(value)
        subject.onCompleted()
    }
    
    subject
  }

  def fromIterable[T](iterable: Iterable[T]):Observable[T] = Observable{subject =>
    iterable foreach{value =>
      if(subject.isUnsubscribed){ break }
      subject.onNext(value)
    }
    subject.onCompleted()
  }
}

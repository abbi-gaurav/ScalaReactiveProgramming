package w3

import scala.async.Async._
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success, Try}

/**
  * Created by gabbi on 21/05/15.
  */
package object futures {

  implicit class FutureAddons[T](val self: Future[T]) {

    def myFallbackTo(that: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
      self recoverWith {
        case _ => that recoverWith { case _ => self }
      }
    }

    def myFilter(p: T => Boolean)(implicit ec: ExecutionContext): Future[T] = async {
      val x = await {
        self
      }
      if (!p(x)) throw new NoSuchElementException()
      else x
    }

    def myFlatMap[S](f: T => Future[S])(implicit ec: ExecutionContext): Future[S] = async {
      val x = await {
        self
      }
      await {
        f(x)
      }
    }

    def withTry()(implicit ec: ExecutionContext): Future[Try[T]] = self map (Success(_)) recover g

    def filterWithPromise(p: T => Boolean)(implicit executionContext: ExecutionContext): Future[T] = {
      val promise: Promise[T] = Promise[T]()
      self onComplete {
        case Success(x) => if (p(x)) promise.success(x) else promise.failure(new NoSuchElementException("filtered"))
        case Failure(err) => promise.failure(err)
      }

      promise.future
    }

    def zipWithPromise[S, R](that: => Future[S], f: (T, S) => R)(implicit executionContext: ExecutionContext): Future[R] = {
      val promise = Promise[R]()
      self onComplete {
        case Success(t) => that onComplete {
          case Success(s) => promise.success(f(t, s))
          case Failure(err) => promise.failure(err)
        }
        case Failure(err) => promise failure err
      }
      promise.future
    }

    def zipWithAwait[S, R](that: => Future[S], f: (T, S) => R)(implicit executionContext: ExecutionContext): Future[R] = async {
      f(await {
        self
      }, await {
        that
      })
    }
  }

  def g[T]: PartialFunction[Throwable, Failure[T]] = {
    case t: Throwable => Failure(t)
  }

  def retry1[T](noOfTimes: Int)(future: => Future[T]): Future[T] = noOfTimes match {
    case 0 => Future.failed(new Exception("sorry"))
    case _ => future fallbackTo {
      retry1(noOfTimes - 1)(future)
    }
  }

  def retryFoldLeft[T](noOfTimes: Int)(future: => Future[T])(implicit ec: ExecutionContext): Future[T] = {
    val attempts: Seq[() => Future[T]] = 1 to noOfTimes map (_ => () => future)
    val failed: Future[T] = Future.failed(new Exception("retries over"))
    val result = attempts.foldLeft(failed) {
      case (acc, currentBlock) => acc recoverWith { case _ => currentBlock() }
    }
    result
  }

  def retryFoldRight[T](noOfTimes: Int)(future: => Future[T]): Future[T] = {
    val attempts: Seq[() => Future[T]] = 1 to noOfTimes map (_ => () => future)
    val failed: Future[T] = Future.failed(new Exception("retries over"))
    val result: () => Future[T] = attempts.foldRight(() => failed) {
      case (currentBlock, acc) => () => currentBlock() fallbackTo acc()
    }
    result()
  }

  def retryAsync[T](noOfTimes: Int)(block: => Future[T])(implicit ec: ExecutionContext): Future[T] = async {
    var i = 0
    var result: Try[T] = scala.util.Failure(new Exception("retries over"))

    while (result.isFailure && i < noOfTimes) {
      result = await {
        block.withTry()
      }
      i += 1
    }
    result.get
  }

  def sequence[T](fts: List[Future[T]])(implicit executionContext: ExecutionContext): Future[List[T]] = fts match {
    case Nil => Future(Nil)
    case (head :: tail) => head.flatMap { t => sequence(tail).flatMap { ts => Future(t :: ts) } }
  }

  def sequenceWithForComp[T](fts: List[Future[T]])(implicit executionContext: ExecutionContext): Future[List[T]] = fts match {
    case Nil => Future(Nil)
    case eventualHead :: eventualTails => for {
      headResult <- eventualHead
      tailResult: List[T] <- sequenceWithForComp(eventualTails)
    } yield headResult :: tailResult
  }

}

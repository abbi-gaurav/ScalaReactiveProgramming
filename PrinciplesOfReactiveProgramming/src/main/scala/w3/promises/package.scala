package w3

import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * Created by gabbi on 31/05/15.
 */
package object promises {
  def race[T](left: Future[T], right: Future[T])(implicit executionContext: ExecutionContext): Future[T] = {
    val p = Promise[T]()
    left onComplete {
      p tryComplete (_)
    }
    right onComplete {
      p tryComplete (_)
    }
    p.future
  }
}

import w3.futures._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

val x: Future[List[Int]] = sequenceWithForComp(List(1, 2, 3).map(Future.successful))

val y = Await.result(x, Duration.Inf)
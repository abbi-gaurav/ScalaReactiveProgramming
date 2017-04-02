package learn.akka.stream.motivatingExample

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by gabbi on 02.04.17.
  */
class Database() {
  private implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  def bulkInsertAsync(messages: Seq[String]): Future[Unit] = Future {
    println(s"inserting bulk ${messages.size} messages")
  }

  def insertAsync(message: String): Unit = println(s"persisting message $message")

}

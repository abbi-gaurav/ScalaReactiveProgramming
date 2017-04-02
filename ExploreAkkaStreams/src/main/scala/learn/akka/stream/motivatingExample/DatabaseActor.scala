package learn.akka.stream.motivatingExample

import akka.actor.Actor
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import learn.akka.stream.motivatingExample.DatabaseActor.{Decrement, Insert, InsertMessage}

import scala.concurrent.duration._

/**
  * Created by gabbi on 02.04.17.
  */
class DatabaseActor extends Actor {
  private val database = new Database()
  private var messages: Seq[String] = Nil
  private var count: Int = 0
  private var flush = true
  private var outstanding = 0
  private implicit val ec = context.dispatcher

  override def preStart(): Unit = context.system.scheduler.scheduleOnce(1 second) {
    self ! Insert
  }

  private val bufferSize = 5

  override def receive: Receive = {
    case InsertMessage(message) =>
      messages = message +: messages
      count += 1
      if (count == bufferSize) {
        flush = false
        insert()
      }

    case Insert =>
      if (flush) insert() else flush = true
      context.system.scheduler.scheduleOnce(1 second) {
        self ! Insert
      }
    case Decrement =>
      outstanding -= 1
      if (count >= bufferSize) {
        insert()
        flush = false
      }
  }

  private def insert(): Unit = {
    if (count > 0 && outstanding < 10) {
      outstanding += 1
      val (insert, remaining) = messages.splitAt(bufferSize)
      messages = remaining
      count = remaining.size

      database.bulkInsertAsync(insert) andThen {
        case _ => self ! Decrement
      }

    }
  }
}

object DatabaseActor {

  case class InsertMessage(message: String)

  object InsertMessage {
    def ack(message: InsertMessage): Message = TextMessage("ack")

    def ack(message: String): Message = TextMessage(s"ack -- $message")

    def parse(text: String): InsertMessage = new InsertMessage(text)
  }

  case object Insert

  case object Decrement

}

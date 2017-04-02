package learn.akka.stream.motivatingExample

import akka.actor.Actor
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import learn.akka.stream.motivatingExample.DatabaseActor.{Insert, InsertMessage}

import scala.concurrent.duration._

/**
  * Created by gabbi on 02.04.17.
  */
class DatabaseActor extends Actor {
  private val database = new Database()
  private var messages: Seq[String] = Nil
  private var count: Int = 0
  private var flush = true
  private implicit val ec = context.dispatcher

  override def preStart(): Unit = context.system.scheduler.scheduleOnce(1 second) {
    self ! Insert
  }

  override def receive: Receive = {
    case InsertMessage(message) =>
      messages = message +: messages
      count += 1
      if (count == 5) {
        flush = false
        insert()
      }

    case Insert =>
      if(flush) insert() else flush = true
      context.system.scheduler.scheduleOnce(1 second) {
        self ! Insert
      }
  }

  private def insert(): Unit = {
    if (count > 0) {
      database.bulkInsertAsync(messages)
      messages = Nil
      count = 0
    }
  }
}

object DatabaseActor {

  case class InsertMessage(message: String)

  object InsertMessage {
    def ack(message: InsertMessage): Message = TextMessage("ack")

    def parse(text: String): InsertMessage = new InsertMessage(text)
  }

  case object Insert

}

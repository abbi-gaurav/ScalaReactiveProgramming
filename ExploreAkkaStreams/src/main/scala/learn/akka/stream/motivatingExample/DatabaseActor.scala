package learn.akka.stream.motivatingExample

import akka.actor.Actor
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import learn.akka.stream.motivatingExample.DatabaseActor.InsertMessage

/**
  * Created by gabbi on 02.04.17.
  */
class DatabaseActor extends Actor {
  private val database = new Database()
  var messages: Seq[String] = Nil
  var count: Int = 0


  override def receive: Receive = {
    case InsertMessage(message) =>
      messages = message +: messages
      count += 1
      if (count == 5) {
        insert()
      }
  }

  private def insert(): Unit = {
    database.bulkInsertAsync(messages)
    messages = Nil
    count = 0
  }
}

object DatabaseActor {

  case class InsertMessage(message: String)

  object InsertMessage {
    def ack(message: InsertMessage): Message = TextMessage("ack")

    def parse(text: String): InsertMessage = new InsertMessage(text)
  }

}

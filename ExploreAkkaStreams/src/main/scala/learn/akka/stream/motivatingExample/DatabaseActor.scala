package learn.akka.stream.motivatingExample

import akka.actor.Actor
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import learn.akka.stream.motivatingExample.DatabaseActor.InsertMessage

/**
  * Created by gabbi on 02.04.17.
  */
class DatabaseActor extends Actor{
  private val database = new Database()
  override def receive: Receive = {
    case InsertMessage(message) => database.insertAsync(message)
  }
}

object DatabaseActor{
  case class InsertMessage(message:String)

  object InsertMessage {
    def ack(message: InsertMessage): Message = TextMessage("ack")

    def parse(text: String): InsertMessage = new InsertMessage(text)
  }

}

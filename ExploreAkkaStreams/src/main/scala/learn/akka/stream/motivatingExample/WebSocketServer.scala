package learn.akka.stream.motivatingExample

import akka.NotUsed
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import learn.akka.stream.motivatingExample.DatabaseActor.InsertMessage

/**
  * Created by gabbi on 02.04.17.
  */
object WebSocketServer extends App with Directives {
  implicit val system = ActorSystem("SimpleTcpServer")
  implicit val materializer = ActorMaterializer()
  private val databaseActor = system.actorOf(Props[DatabaseActor], "database")

  val measurementsWebSocketService: Flow[Message, Message, NotUsed] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) =>
          val message = InsertMessage.parse(text)
          databaseActor ! message
          InsertMessage.ack(message)
      }
  val route = path("measurement") {
    get {
      handleWebSocketMessages(measurementsWebSocketService)
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}

package learn.akka.stream.integration.part2

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import learn.akka.stream.motivatingExample.DatabaseActor.InsertMessage

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by gabbi on 02.04.17.
  */
object EchoWebSocket extends App with Directives {
  private implicit val system = ActorSystem("AkkaStreams")
  private implicit val materializer = ActorMaterializer()
  private implicit val executionContext = system.dispatcher

  private val echoWebSocketService =
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => Future.successful(text)
        case TextMessage.Streamed(textStream) =>
          textStream.runFold("")(_ + _).flatMap(Future.successful)
      }
      .mapAsync(1)(identity)
      .map(str => TextMessage(str))

  val route: Route = path("echo" / Segment) { (id: String) =>
    get {
      handleWebSocketMessages(echoWebSocketService)
    }
  }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}

package learn.akka.stream.integration

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import learn.akka.stream.integration.Total.{CurrentTotal, GetTotal, Increment}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by gabbi on 23.04.17.
  */
object MeasurementWebSocket extends App with Directives {
  private implicit val system: ActorSystem = ActorSystem("MeasurementWebSocket")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private val total: ActorRef = system.actorOf(Props[Total], "total")

  val measurementsWebSocket: Flow[Message, Message, NotUsed] =
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => Future.successful(text)
        case TextMessage.Streamed(stream) => stream.runFold("")(_ + _).flatMap(Future.successful)
      }
      .mapAsync(parallelism = 1)(f = identity)
      .groupedWithin(n = 1000, d = 1 second)
      .map(messages => (messages.last, Messages.parse(messages)))
      .map {
        case (lastMessage, measurements) =>
          total ! Increment(measurements.sum)
          lastMessage
      }
      .map(Messages.ack)

  val route: Route =
    path("measurements") {
      get {
        handleWebSocketMessages(measurementsWebSocket)
      }
    } ~ path("total") {
      get {
        import akka.pattern.ask
        implicit val askTimeout = Timeout(30 seconds)
        onSuccess(total ? GetTotal) {
          case CurrentTotal(total) =>
            complete(s"The total is : $total")
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}

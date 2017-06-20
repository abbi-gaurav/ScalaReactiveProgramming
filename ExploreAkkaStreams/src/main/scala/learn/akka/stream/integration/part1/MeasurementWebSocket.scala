package learn.akka.stream.integration.part1

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.Timeout
import learn.akka.stream.integration.part1.Total._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by gabbi on 23.04.17.
  */
object MeasurementWebSocket extends App with Directives {
  private implicit val system: ActorSystem = ActorSystem("MeasurementWebSocket")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()
  private implicit val executionContext = system.dispatcher
  private val total: ActorRef = system.actorOf(Props[Total], "total")

  val measurementsWebSocket = (sink: Sink[Increment, NotUsed]) =>
    Flow[Message]
      .collect {
        case TextMessage.Strict(text) => Future.successful(text)
        case TextMessage.Streamed(stream) => stream.runFold("")(_ + _).flatMap(Future.successful)
      }
      .mapAsync(parallelism = 1)(f = identity)
      .groupedWithin(n = 1000, d = 1 second)
      .map(Messages.parse)
      .map(measurements => Increment(measurements.sum, measurements.last))
      .alsoTo(sink)
      .map(increment => Messages.ack(increment.lastMessage))

  val route: Route =
    path("measurements" / LongNumber) { id =>
      get {
        val sink = Sink.actorRefWithAck(total, Init, Ack, Complete(id))
        handleWebSocketMessages(measurementsWebSocket(sink))
      }
    } ~ path("total") {
      get {
        import akka.pattern.ask
        implicit val askTimeout = Timeout(30 seconds)
        onSuccess(total ? GetTotal) {
          case CurrentTotal(totalValue) =>
            complete(s"The total is : $totalValue")
        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
}

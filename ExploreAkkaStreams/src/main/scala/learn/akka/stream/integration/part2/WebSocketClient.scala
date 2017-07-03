package learn.akka.stream.integration.part2

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream._
import akka.stream.scaladsl.GraphDSL.Builder
import akka.stream.scaladsl.{Flow, GraphDSL, Keep, Sink, Source}
import akka.{Done, NotUsed}
import learn.akka.stream.integration.part2.WindTurbineSimulator._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Created by gabbi on 25.06.17.
  */
object WebSocketClient {
  def apply(id: String, endpoint: String, supervisor: ActorRef)
           (implicit system: ActorSystem,
            materializer: ActorMaterializer,
            executionContext: ExecutionContext): WebSocketClient = new WebSocketClient(id, endpoint, supervisor)
}

class WebSocketClient(id: String, endpoint: String, supervisor: ActorRef)
                     (implicit system: ActorSystem,
                      materializer: ActorMaterializer,
                      executionContext: ExecutionContext) {
  private val webSocket: Flow[Message, Message, Future[WebSocketUpgradeResponse]] = {
    val webSocketUri = s"$endpoint/$id"
    Http().webSocketClientFlow(WebSocketRequest(uri = webSocketUri))
  }

  private val outgoing: Graph[SourceShape[TextMessage.Strict], NotUsed] = GraphDSL.create() { implicit builder: Builder[NotUsed] =>
    val data: WindTurbineData = WindTurbineData(id)
    val flow: SourceShape[TextMessage.Strict] = builder.add {
      Source.tick(1 second, 1 seconds, ())
        .map(_ => TextMessage(data.getNext))
    }

    SourceShape(flow.out)
  }

  private val incoming: Graph[FlowShape[Message, Unit], NotUsed] = GraphDSL.create() { implicit builder =>
    val flow: FlowShape[Message, Unit] = builder.add {
      Flow[Message]
        .collect {
          case TextMessage.Strict(text) => Future.successful(text)
          case TextMessage.Streamed(textStream) => textStream.runFold("")(_ + _).flatMap(Future.successful)
        }
        .mapAsync(1)(identity)
        .map(println)
    }
    FlowShape(flow.in, flow.out)
  }

  val ((eventualUpgradedResponse: Future[WebSocketUpgradeResponse], killSwitch: UniqueKillSwitch), eventualDone: Future[Done]) =
    Source
      .fromGraph(outgoing)
      .viaMat(webSocket)(Keep.right)
      .viaMat(KillSwitches.single)(Keep.both)
      .via(incoming)
      .toMat(Sink.ignore)(Keep.both)
      .run()

  private val connected: Future[Unit] =
    eventualUpgradedResponse.map { (upgradedResponse: WebSocketUpgradeResponse) =>
      upgradedResponse.response.status match {
        case StatusCodes.SwitchingProtocols => supervisor ! Upgraded
        case statusCode => supervisor ! FailedUpgrade(statusCode = statusCode)
      }
    }

  connected.onComplete {
    case Success(_) => supervisor ! Connected
    case Failure(ex) => supervisor ! ConnectionFailure(ex)
  }

  eventualDone map (_ => supervisor ! Terminated)
}

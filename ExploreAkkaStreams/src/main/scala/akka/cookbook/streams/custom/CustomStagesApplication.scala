package akka.cookbook.streams.custom

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object CustomStagesApplication extends App {
  implicit val actorSystem: ActorSystem = ActorSystem("custom-stage-application")
  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()

  val source: Source[String, NotUsed] = Source.fromGraph(new HelloAkkaStreamsSource)
  val upperCaseMapper: Flow[String, String, NotUsed] = Flow[String].map(_.toUpperCase)
  val splitter = Flow[String].mapConcat(_.split(" ").toList)

  val punctuationMapper = Flow[String].map(_.replaceAll("""[\p{Punct}&&[^.]]""", "").replaceAll(System.lineSeparator(), ""))

  val filterEmptyElements = Flow[String].filter(_.nonEmpty)
  val sink = Sink.fromGraph(new WordCounterSink)

  val stream = source
    .via(upperCaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .to(sink)

  stream.run()
}

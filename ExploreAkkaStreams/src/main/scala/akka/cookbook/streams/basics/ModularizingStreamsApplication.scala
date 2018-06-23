package akka.cookbook.streams.basics

import java.nio.file.Paths

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Compression, FileIO, Flow, Sink, Source}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

object ModularizingStreamsApplication extends App {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  private val maxGroups = 100
  private val path = Paths.get("src/main/resources/gzipped-file.gz")

  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(path)
  val gunzip: Flow[ByteString, ByteString, NotUsed] = Flow[ByteString].via(Compression.gunzip())

  val utf8UpperCaseMapper: Flow[ByteString, String, NotUsed] = Flow[ByteString].map(_.utf8String.toUpperCase)
  val utf8LowerCaseMapper: Flow[ByteString, String, NotUsed] = Flow[ByteString].map(_.utf8String.toLowerCase)
  val splitter: Flow[String, String, NotUsed] = Flow[String].mapConcat(_.split(" ").toList)
  val punctuationMapper = Flow[String].map(_.replaceAll("""[\p{Punct}&&[^.]]""", "").replaceAll(System.lineSeparator(), ""))
  val filterEmptyElements: Flow[String, String, NotUsed] = Flow[String].filter(_.nonEmpty)
  val wordCountFlow = Flow[String]
    .groupBy(maxGroups, identity)
    .map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams

  val sink = Sink.foreach(println)

  val streamUpperCase = source
    .via(gunzip)
    .via(utf8UpperCaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .via(wordCountFlow)
    .to(sink)

  val streamLowerCase = source
    .via(gunzip)
    .via(utf8LowerCaseMapper)
    .via(splitter)
    .via(punctuationMapper)
    .via(filterEmptyElements)
    .via(wordCountFlow)
    .to(sink)

  streamUpperCase.run()
  streamLowerCase.run()
}

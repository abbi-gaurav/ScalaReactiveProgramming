package akka.cookbook.streams

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult}
import akka.stream.scaladsl.{Compression, FileIO, RunnableGraph, Sink}

import scala.concurrent.Future

object TransformingStreamsApplication extends App {
  private implicit val actorSystem: ActorSystem = ActorSystem()
  private implicit val actorMaterializer: ActorMaterializer = ActorMaterializer()
  private val maxGroups = 100
  private val path = Paths.get("src/main/resources/gzipped-file.gz")

  private val stream: RunnableGraph[Future[IOResult]] = FileIO.fromPath(path)
    .via(Compression.gunzip())
    .map(_.utf8String.toUpperCase)
    .mapConcat(_.split(" ").toList)
    .collect { case w: String if w.nonEmpty =>
      w
        .replaceAll("""[p{Punct}&&[^.]]""", "")
        .replaceAll(System.lineSeparator(), "")
    }
    .groupBy(maxGroups, identity)
    .map(x => (x, 1)) //.map(_ -> 1)
    .reduce((l, r) => (l._1, l._2 + r._2))
    .mergeSubstreams
    .to(Sink.foreach(println))

  stream.run()
}

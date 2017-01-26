package learn.akka.stream.qs

/**
  * Created by gabbi on 21/12/2016.
  */

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent._

object BasicExample extends App {
  private implicit val system: ActorSystem = ActorSystem("BasicExample")
  private implicit val materializer: ActorMaterializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)
  val x: Future[Done] = source.runForeach(println(_))(materializer = materializer)

  val factorials: Source[BigInt, NotUsed] = source.scan(BigInt(1))((acc, next) => acc * next)

  val result: Future[IOResult] = factorials
    .map(num => ByteString(s"$num\n"))
    .runWith(FileIO.toPath(Paths.get("target", "facts.txt")))

  def lineSink(fileName: String): Sink[String, Future[IOResult]] = Flow[String]
    .map(s => ByteString(s"$s\n"))
    .toMat(FileIO.toPath(Paths.get("target", fileName)))(Keep.right)

  factorials.map(_.toString).runWith(lineSink("fact2.txt"))
}

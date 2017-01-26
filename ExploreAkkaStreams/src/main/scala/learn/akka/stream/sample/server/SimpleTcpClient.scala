package learn.akka.stream.sample.server

import akka.NotUsed
import akka.actor._
import akka.stream._
import akka.stream.scaladsl.Tcp.OutgoingConnection
import akka.stream.scaladsl._
import akka.util._

import scala.concurrent.Future
import scala.io.StdIn

/**
  * Created by gabbi on 08/01/2017.
  */
object SimpleTcpClient extends App {
  implicit val clientActor: ActorSystem = ActorSystem("simpleTCPClient")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val address: String = "localhost"
  val port: Int = 6666

  val connection: Flow[ByteString, ByteString, Future[OutgoingConnection]] = Tcp().outgoingConnection(address, port)

  val sink = Sink.foreach[ByteString](x => println(x.utf8String))
  val s = Source(1 to 3).map(_ + "\n").map(ByteString(_))

  val flow: Flow[ByteString, ByteString, NotUsed] = Flow[ByteString]
    .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 256, allowTruncation = true))
    .map(_.utf8String)
    .map(text => println("Server: " + text))
    .map(_ => StdIn.readLine("> "))
    .map(_ + "\n")
    .map(x => ByteString(x))

  connection.join(flow).run()
}

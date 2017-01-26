package learn.akka.stream.sample.server

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Framing, Sink, Source, Tcp}
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import akka.{Done, NotUsed}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by gabbi on 08/01/2017.
  */
object SimpleTcpServer extends App {

  val address = "localhost"
  val port = 6666

  def serverLogic(connection: IncomingConnection): Flow[ByteString, ByteString, NotUsed] = {

    val welcome: Source[String, NotUsed] = Source.single(s"you are connected to ${connection.localAddress} from ${connection.remoteAddress}")

    val delimiter: Flow[ByteString, ByteString, NotUsed] = Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true
    )

    val converter: Flow[ByteString, String, NotUsed] = Flow[ByteString].map { (bytes: ByteString) =>
      val message = bytes.utf8String
      println(s"server received message $message")
      message
    }

    val responder: Flow[String, ByteString, NotUsed] = Flow[String].map { string =>
      val answer: String = s"Server responded with message [$string]\n"
      ByteString(answer)
    }

    Flow[ByteString]
      .via(delimiter)
      .via(converter)
      .merge(welcome)
      .via(responder)
  }

  def mkServer(address: String, port: Int)(implicit system: ActorSystem, materializer: Materializer): Unit = {
    import system.dispatcher
    val connectionHandler: Sink[IncomingConnection, Future[Done]] = Sink.foreach[Tcp.IncomingConnection] { (conn: IncomingConnection) =>
      println(s"incomig connection from ${conn.remoteAddress}")
      conn.handleWith(serverLogic(conn))
    }

    val incomingConnections: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(address, port)

    val binding: Future[ServerBinding] = incomingConnections.to(connectionHandler).run()

    binding onComplete {
      case Success(b) => println(s"server started on ${b.localAddress}")
      case Failure(e) => println(s"failed to start server ${e.getLocalizedMessage}")
    }
  }

  def mkAkkaServer(): Unit = {
    implicit val server = ActorSystem("SimpleTcpServer")
    implicit val materializer = ActorMaterializer()
    mkServer(address, port)
  }

  mkAkkaServer()
}

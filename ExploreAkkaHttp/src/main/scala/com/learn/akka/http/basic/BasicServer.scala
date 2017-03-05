package com.learn.akka.http.basic

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.io.StdIn

/**
  * Created by gabbi on 19/02/2017.
  */
object BasicServer {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("basic-server")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      path("hello") {
        get {
          complete(HttpEntity(ContentTypes.`application/json`,s"""["hello","from","akka-http"]"""))
        }
      }

    val binding: Future[ServerBinding] = Http().bindAndHandle(route,"localhost", 8080)

    println(s"Server running at http://localhost:9000. Press RETURN to stop...")

    StdIn.readLine()

    binding
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

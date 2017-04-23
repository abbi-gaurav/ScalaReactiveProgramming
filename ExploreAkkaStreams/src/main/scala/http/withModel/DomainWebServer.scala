package http.withModel

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.pattern._
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.Future
import scala.io.StdIn

object DomainWebServer extends Directives with JsonSupport {
  private implicit val system = ActorSystem()
  private implicit val timeout = Timeout(1, TimeUnit.SECONDS)
  private val db = system.actorOf(Props[DB])
  private implicit val executionContext = system.dispatcher

  // (fake) async database query api
  def fetchItem(itemId: Long): Future[Option[Item]] = db ? DB.Fetch(itemId) collect {
    case Some(item: Item) => Some(item)
    case _ => None
  }

  def saveOrder(order: Order): Future[Done] = db ? DB.Add(order.items) collect {
    case Done => Done
  }

  def main(args: Array[String]) {
    // needed to run the route

    implicit val materializer = ActorMaterializer()
    // needed for the future map/flatmap in the end

    val route: Route =
      get {
        pathPrefix("items" / LongNumber) { id =>
          // there might be no item for a given id
          val maybeItem: Future[Option[Item]] = fetchItem(id)

          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None => complete(StatusCodes.NotFound)
          }
        }
      } ~
        post {
          path("items") {
            entity(as[Order]) { order =>
              val saved: Future[Done] = saveOrder(order)
              onComplete(saved) { done =>
                complete("order created")
              }
            }
          }
        }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done

  }
}
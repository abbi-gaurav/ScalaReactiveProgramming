package learn.akka.stream.largeDataSets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoCollection}
import org.bson.Document
import org.reactivestreams.Publisher

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

/**
  * Created by gabbi on 25.06.17.
  */
object ApiReturningList extends App with ServiceDirectives {
  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val mongoClient: MongoClient = MongoClients.create()
  private val collection = mongoClient.getDatabase("test").getCollection("resources")

  private def getData(collection: MongoCollection[Document], pageParams: PageParams): Future[String] = {
    val publisher: Publisher[Document] = collection.find().skip(pageParams.skip).limit(pageParams.limit)

    Source.fromPublisher(publisher)
      .map(_.toJson)
      .intersperse("[", ",", "]")
      .runFold("")(_ + _)
  }

  private val route = path("resources") {
    pageParams { pageParams =>
      get {
        complete(getData(collection, pageParams).map(HttpEntity(ContentTypes.`application/json`, _)))
      }
    }
  }

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

package learn.akka.stream.largeDataSets

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.mongodb.reactivestreams.client.{MongoClient, MongoClients, MongoCollection}
import org.bson.Document
import org.reactivestreams.Publisher

import scala.concurrent.ExecutionContext
import scala.io.StdIn

/**
  * Created by gabbi on 25.06.17.
  */
object ApiReturningChunking extends App with ServiceDirectives {
  private implicit val system = ActorSystem()
  private implicit val materializer = ActorMaterializer()
  private implicit val executionContext: ExecutionContext = system.dispatcher

  private val mongoClient: MongoClient = MongoClients.create()
  private val collection = mongoClient.getDatabase("test").getCollection("resources")

  private def getData(collection: MongoCollection[Document]): Source[ByteString, NotUsed] = {
    val publisher: Publisher[Document] = collection.find()

    Source.fromPublisher(publisher)
      .map(_.toJson)
      .map(ByteString.apply)
      .intersperse(ByteString("["), ByteString(","), ByteString("]"))
  }

  private val route = path("resources") {
    get {
      complete(HttpEntity(contentType = ContentTypes.`application/json`, data = getData(collection)))
    }
  }

  private val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}

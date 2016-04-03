package w5.urlFetcher

import java.util.concurrent.Executor

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.Future
import scala.util.Success

/**
 * Created by gabbi on 23/06/15.
 */
object GetterSpec {
  val firstLink = "http://abbi.gaurav/1"

  val bodies: Map[String, String] = Map(
    firstLink ->
      """<html>
        | <head><title>Page 1</title></head>
        | <body>
        |   <h1>A Link</h1>
        |   <a href="http://abbi.gaurav/2">click here</a>
        | </body>
        | </html>
      """.stripMargin
  )

  val links = Map(firstLink -> Seq("http://abbi.gaurav/2"))

  object FakeWebClient extends WebClient{
    override def get(url: String)(implicit executor: Executor): Future[String] = bodies get url match {
      case None => Future.failed[String](BadStatus(404))
      case Some(value) => Future.successful(value)
    }

    override def shutdown(): Unit = ()
  }

  def fakeGetter(url:String, depth:Int):Props = Props(new Getter(url, depth){
    override def client:WebClient = FakeWebClient
  })
}

class GetterSpec extends TestKit(ActorSystem("GetterSpec")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender{
  import GetterSpec._
  override def afterAll():Unit = {
    system.shutdown()
  }

  "A Getter" must {
    "return the right body" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter(firstLink,2),testActor)), "rightbody")
      for(link <- links(firstLink)){
        expectMsg(Controller.Check(link,2))
      }
      expectMsg(Getter.Done)
    }

    "properly finish in case of errors" in {
      val getter = system.actorOf(Props(new StepParent(fakeGetter("unknown",2),testActor)), "wrongLink")

      expectMsg(Getter.Done)
    }
  }
}

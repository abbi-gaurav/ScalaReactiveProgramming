package structure

import java.util.Date

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps

class ScatterGatherTest extends TestKit(ActorSystem("scatter-gather"))
  with WordSpecLike
  with BeforeAndAfterAll {
  private val timeout = 2 seconds

  override protected def afterAll(): Unit = system.terminate()

  "The scatter gather" must {
    "scatter the message and gather them again" in {
      val endProbe = TestProbe()
      val aggregatorRef = system.actorOf(
        Props(new Aggregator(timeout, endProbe.ref))
      )
      val speedRef = system.actorOf(Props(new GetSpeed(aggregatorRef)))
      val timeRef = system.actorOf(Props(new GetTime(aggregatorRef)))
      val recipientListRef = system.actorOf(Props(new RecipientList(List(speedRef, timeRef))))
      val photoDate = new Date()
      val speed = 60
      val photoMessage = PhotoMessage(
        "id1",
        ImageProcessing.createPhotoString(photoDate, speed)
      )

      recipientListRef ! photoMessage

      val combinedMessage = photoMessage.copy(creationTime = Some(photoDate), speed = Some(speed))

      endProbe.expectMsg(combinedMessage)
    }
  }
}

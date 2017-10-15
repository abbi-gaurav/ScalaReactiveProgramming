package routing.routers

import java.util.Date

import akka.actor.{ActorSystem, DeadLetter, Props}
import akka.routing.RoundRobinGroup
import akka.testkit.{TestKit, TestProbe}
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import structure.ImageProcessing

import scala.concurrent.duration._
import scala.language.postfixOps

class PerfRoutingTest extends TestKit(ActorSystem("perf-routing"))
  with WordSpecLike
  with StopSystemAfterAll
  with MustMatchers {

  "The router group" must {
    "use recreated routees" in {
      val endProbe = TestProbe()
      val deadProbe = TestProbe()
      system.eventStream.subscribe(
        deadProbe.ref,
        classOf[DeadLetter])

      val creator = system.actorOf(Props(new GetLicenseCreator2(2, endProbe.ref)), "GetLicenseCreator-test5")
      val paths = List(
        "/user/GetLicenseCreator-test5/GetLicense0",
        "/user/GetLicenseCreator-test5/GetLicense1"
      )
      val router = system.actorOf(RoundRobinGroup(paths).props(), "router-test5")
      creator ! "KillFirst"
      Thread.sleep(100)

      val msg = PerformanceRoutingMessage(
        ImageProcessing.createPhotoString(new Date(), 60, "123xyz"),
        None,
        None
      )

      router ! msg

      val procMsg = endProbe.expectMsgType[PerformanceRoutingMessage](1 seconds)
      println(s"Received msg $procMsg")
      endProbe.expectNoMsg()
      deadProbe.expectNoMsg()
    }
  }
}

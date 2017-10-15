package routing.routers

import java.util.Date

import akka.actor.{ActorRef, ActorSystem, DeadLetter, Props}
import akka.routing.FromConfig
import akka.testkit.{TestKit, TestProbe}
import base.StopSystemAfterAll
import com.typesafe.config.Config
import org.scalatest.{MustMatchers, WordSpecLike}
import structure.ImageProcessing

import scala.concurrent.duration._
import scala.language.postfixOps

abstract class PoolTest(name: String, config: Config)
  extends TestKit(ActorSystem(name, config))
    with WordSpecLike
    with MustMatchers
    with StopSystemAfterAll {
  "Pool" must {
    "create router as per configuration" in {
      val endProbe = TestProbe()
      val deadProbe = TestProbe()
      system.eventStream.subscribe(deadProbe.ref, classOf[DeadLetter])

      val router: ActorRef = system.actorOf(
        FromConfig.props(Props(new GetLicense(endProbe.ref))),
        "poolRouter"
      )
      println(router)
      Thread.sleep(100)

      val performanceRoutingMessage = PerformanceRoutingMessage(
        ImageProcessing.createPhotoString(new Date(), 60, "123xzy"),
        None,
        None
      )
      router ! performanceRoutingMessage

      val procMsg = endProbe.expectMsgType[PerformanceRoutingMessage](1 seconds)
      println(s"Received: $procMsg")
      endProbe.expectNoMsg()
      deadProbe.expectNoMsg()
    }
  }

}

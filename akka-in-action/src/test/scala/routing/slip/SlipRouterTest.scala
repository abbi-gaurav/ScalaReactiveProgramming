package routing.slip

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

class SlipRouterTest extends TestKit(ActorSystem("slip-router"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {
  val probe = TestProbe()
  val router: ActorRef = system.actorOf(Props(new SlipRouter(probe.ref)), "Sliprouter")
  "A slip router" must {
    "create a default order car" in {
      router ! Order(Seq.empty)

      val defaultCar = Car(color = "black")
      probe.expectMsg(defaultCar)
    }
    "create car with all options" in {
      val fullOrder = Order(Seq(
        CarOptions.COLOR_GRAY,
        CarOptions.PARKING_SENSORS,
        CarOptions.NAVIGATION
      ))
      router ! fullOrder
      val highEndCar = Car(
        color = "gray",
        hasNavigation = true,
        hasParkingSensors = true
      )
      probe.expectMsg(highEndCar)
    }
  }
}

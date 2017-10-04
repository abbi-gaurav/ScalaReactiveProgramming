package scenarios

import akka.actor.{ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import scenarios.SilentActor.{GetState, SilentMessage}

class SilentActorTest01 extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {
  "A Silent actor" must {
    "change state when it receives message, multi-threaded" in {
      val silentActor = system.actorOf(Props[SilentActor])
      silentActor ! SilentMessage("w1")
      silentActor ! SilentMessage("w2")

      silentActor ! GetState(testActor)
      expectMsg(Vector("w1","w2"))
    }
  }
}

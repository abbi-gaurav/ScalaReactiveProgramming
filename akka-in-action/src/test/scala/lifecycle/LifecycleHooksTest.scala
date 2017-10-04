package lifecycle

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import scenarios.lifecycle.LifecycleHooks

class LifecycleHooksTest extends TestKit(ActorSystem("lifecycle-hooks"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {
  "Lifecycle Hooks" must {
    "follow actor lifecycle as expected" in {
      val actor: ActorRef = system.actorOf(Props[LifecycleHooks], "lifecycle-hooks")
      actor ! "restart"
      actor.tell("msg", testActor)
      expectMsg("msg")
      system.stop(actor)
      Thread.sleep(1000)
    }
  }
}

package w5.testing

import akka.actor.{Props, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.junit.Test
import scala.concurrent.duration._
/**
 * Created by gabbi on 23/06/15.
 */
class UsingTestProbe{
  private implicit val actorSystem = ActorSystem("TestSys")

  @Test
  def testToggle(): Unit ={
    val toggle = actorSystem.actorOf(Props[Toggle])
    val probe = TestProbe()
    probe.send(toggle, "How are you")
    probe.expectMsg("happy")
    probe.send(toggle, "How are you")
    probe.expectMsg("sad")
    probe.send(toggle, "unknown")
    probe.expectNoMsg(1.second)
    actorSystem.shutdown()
  }

  @Test
  def testToggle2():Unit = {
    new TestKit(actorSystem) with ImplicitSender{
      val toggle = system.actorOf(Props[Toggle])
      toggle ! "How are you"
      expectMsg("happy")
      toggle ! "How are you"
      expectMsg("sad")
      toggle ! "How are youww"
      expectNoMsg(1 second)
      system shutdown
    }
    ???
  }
}

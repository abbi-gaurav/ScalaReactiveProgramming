package w5.urlFetcher

import akka.actor.Actor.Receive
import akka.actor.{Props, Actor, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import w5.urlFetcher.Receptionist.{Failed, Get, Result}
import scala.concurrent.duration._
/**
 * Created by gabbi on 23/06/15.
 */
object ReceptionistSpec{
  class FakeController extends Actor{
    import context.dispatcher
    override def receive: Receive = {
      case Controller.Check(url, depth) =>
        context.system.scheduler.scheduleOnce(1 second, sender(), Controller.Result(Set(url)))
    }
  }

  def fakeReceptionist:Props = Props(new Receptionist(){
    override def controllerProps = Props[FakeController]
  })
}
class ReceptionistSpec extends TestKit(ActorSystem("ReceptionistSpec")) with WordSpecLike with BeforeAndAfterAll with ImplicitSender{
  import ReceptionistSpec._

  override def afterAll():Unit = system.shutdown()

  "A receptionist" must {
    "reply with a result" in {
      val receptionist = system.actorOf(fakeReceptionist, "sendResult")
      receptionist ! Receptionist.Get("myUrl")
      expectMsg(Result("myUrl",Set("myUrl")))
    }

    "fail when flodded" in {
      val receptionist = system.actorOf(fakeReceptionist, "floddedOne")
      for(i <- 1 to 5) receptionist ! Get(s"myUrl$i")
      expectMsg(Failed("myUrl5"))
      for(i <- 1 to 4) expectMsg(Result(s"myUrl$i", Set(s"myUrl$i")))
    }
  }
}

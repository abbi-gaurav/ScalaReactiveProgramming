package scenarios

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.TestKit
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.collection.immutable

class FilteringActorTest extends TestKit(ActorSystem("test-system"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {

  import FilteringActor._

  "A Filtering Actor" must {
    "filter out particular messages" in {
      val props = FilteringActor.props(testActor, 5)
      val filter: ActorRef = system.actorOf(props, "filter-1")

      filter ! Event(1)
      filter ! Event(2)
      filter ! Event(1)
      filter ! Event(3)
      filter ! Event(1)
      filter ! Event(4)
      filter ! Event(5)
      filter ! Event(5)
      filter ! Event(6)

      val eventsId: immutable.Seq[Long] = receiveWhile() {
        case Event(id) if id <= 5 => id
      }

      eventsId mustBe (1 to 5)

      expectMsg(Event(6))
    }
  }
}

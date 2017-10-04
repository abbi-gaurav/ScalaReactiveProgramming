package scenarios

import akka.actor.ActorSystem
import akka.testkit.TestKit
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import scenarios.SendingActor.{Event, SortEvents, SortedEvents}

import scala.util.Random

class SendingActorTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll {
  "A Sending Actor" must {
    "send message to another actor when it finished processing" in {
      val props = SendingActor.props(testActor)
      val sendingActor = system.actorOf(props, "sendingActor")
      val size = 100
      val maxInclusive = 100000

      def randomEvents = (0 until size).map(_ => Event(Random.nextInt(maxInclusive))).toVector

      val unsorted = randomEvents
      sendingActor ! SortEvents(unsorted)

      expectMsgPF(){
        case SortedEvents(sortedEvents) =>
          sortedEvents.size mustBe unsorted.size
          unsorted.sortBy(_.id) mustBe sortedEvents
      }
    }
  }
}

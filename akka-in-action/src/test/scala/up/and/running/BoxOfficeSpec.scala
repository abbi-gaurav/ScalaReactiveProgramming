package up.and.running

import akka.actor.{ActorSystem, Props}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}
import up.and.running.BoxOffice._
import up.and.running.TicketSeller.{Add, Ticket, Tickets}

import scala.language.postfixOps

class BoxOfficeSpec extends TestKit(ActorSystem("box-office-spec"))
  with WordSpecLike
  with MustMatchers
  with StopSystemAfterAll
  with DefaultTimeout
  with ImplicitSender {

  "The Box office" must {
    "create an event and get tickets from the correct ticket seller" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val eventName = "RHCP"
      boxOffice ! CreateEvent(eventName, 10)
      expectMsg(EventCreated(Event(eventName, 10)))
      boxOffice ! GetEvents
      expectMsg(Events(Vector(Event(eventName, 10))))

      boxOffice ! GetTickets(eventName, 1)
      expectMsg(Tickets(eventName, Vector(Ticket(1))))

      boxOffice ! GetTickets("No event", 1)
      expectMsg(Tickets("No event"))
    }

    "create a child actor when event is created and sends it a Ticket message" in {
      val boxOffice = system.actorOf(Props(new BoxOffice() {
        override private[running] def createTicketSeller(event: String) = testActor
      }))

      val tickets = 3
      val eventName = "RHCP"
      val expectedTickets = (1 to tickets).map(Ticket).toVector
      boxOffice ! CreateEvent(eventName, tickets)
      expectMsg(Add(expectedTickets))
      expectMsg(EventCreated(Event(eventName, tickets)))
    }

    "get and cancel an event that is not created yet" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val notExistingEvent = "doesNotExist"
      boxOffice ! GetEvent(notExistingEvent)
      expectMsg(None)

      boxOffice ! CancelEvent(notExistingEvent)
      expectMsg(None)
    }

    "cancel a created event" in {
      val boxOffice = system.actorOf(BoxOffice.props)
      val tickets = 10
      val event = "RHCP"

      boxOffice ! CreateEvent(event, tickets)
      expectMsg(EventCreated(Event(event, 10)))

      boxOffice ! CancelEvent(event)
      expectMsg(Some(Event(event, 10)))
    }
  }
}

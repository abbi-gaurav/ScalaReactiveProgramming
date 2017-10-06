package api.common.actors

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

import scala.collection.immutable
import scala.concurrent.{ExecutionContextExecutor, Future}

object BoxOffice {
  def props(implicit timeout: Timeout): Props = Props(new BoxOffice)

  def name: String = "boxOffice"

  case class CreateEvent(name: String, tickets: Int)

  case class GetEvent(name: String)

  case object GetEvents

  case class GetTickets(event: String, tickets: Int)

  case class CancelEvent(event: String)

  case class Event(name: String, tickets: Int)

  case class Events(events: Vector[Event])

  sealed trait EventResponse

  case class EventCreated(event: Event) extends EventResponse

  case object EventExists extends EventResponse

}

class BoxOffice(implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  private[actors] def createTicketSeller(event: String): ActorRef = context.actorOf(TicketSeller.props(event), name = event)

  override def receive: Receive = {
    case CreateEvent(name, tickets) =>
      def create(): Unit = {
        val eventTickets: ActorRef = createTicketSeller(event = name)
        val newTickets: Vector[TicketSeller.Ticket] = (1 to tickets).map(TicketSeller.Ticket).toVector
        eventTickets ! TicketSeller.Add(newTickets)
        sender() ! EventCreated(Event(name = name, tickets = tickets))
      }

      context.child(name).fold(create())(_ => sender() ! EventExists)

    case GetEvent(event) =>
      def notFound(): Unit = sender() ! None

      def getEvent(child: ActorRef): Unit = child.forward(TicketSeller.GetEvent)

      context.child(event).fold(notFound())(getEvent)

    case GetTickets(event, tickets) =>
      def notFound(): Unit = sender() ! TicketSeller.Tickets(event)

      def buy(child: ActorRef): Unit = child.forward(TicketSeller.Buy(tickets))

      context.child(event).fold(notFound())(buy)

    case GetEvents =>
      import akka.pattern._
      implicit val ec: ExecutionContextExecutor = context.dispatcher
      def getEvents: immutable.Iterable[Future[Option[Event]]] = {
        context.children.map(child => self.ask(GetEvent(child.path.name)).mapTo[Option[Event]])
      }

      def convertToEvents(f: Future[Iterable[Option[Event]]]) = {
        val eventualEvents: Future[Iterable[Event]] = f.map(_.flatten)
        eventualEvents.map(i => Events(i.toVector))
      }

      pipe(convertToEvents(Future.sequence(getEvents))) to sender()

    case CancelEvent(event) =>
      def notFound(): Unit = sender() ! None

      def cancel(child: ActorRef): Unit = child.forward(TicketSeller.Cancel)

      context.child(event).fold(notFound())(cancel)
  }
}

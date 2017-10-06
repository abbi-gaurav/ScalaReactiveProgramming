package api.common.actors

import akka.actor.{Actor, PoisonPill, Props}

object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))

  case class Ticket(id: Int)

  case class Add(tickets: Vector[Ticket])

  case class Buy(tickets: Int)

  case class Tickets(event: String, entries: Vector[Ticket] = Vector.empty)

  case object GetEvent

  case object Cancel

}

class TicketSeller(event: String) extends Actor {

  import TicketSeller._

  var tickets: Vector[Ticket] = Vector.empty

  override def receive: Receive = {
    case Add(newTickets) => tickets = tickets ++ newTickets
    case Buy(numberOfTickets) =>
      val entries: Vector[Ticket] = tickets take numberOfTickets
      if (entries.size >= numberOfTickets) {
        sender() ! Tickets(event = event, entries = entries)
        tickets = tickets drop numberOfTickets
      } else {
        sender() ! Tickets(event = event)
      }
    case GetEvent => sender() ! Some(BoxOffice.Event(name = event, tickets = tickets.size))
    case Cancel =>
      sender() ! Some(BoxOffice.Event(name = event, tickets = tickets.size))
      self ! PoisonPill
  }
}

package api.common

import akka.actor.ActorRef
import akka.pattern._
import akka.util.Timeout
import api.common.actors.TicketSeller

import scala.concurrent.{ExecutionContext, Future}

trait BoxOfficeApi {

  import actors.BoxOffice._

  def createBoxOffice(): ActorRef

  implicit def executionContext: ExecutionContext

  implicit def requestTimeout: Timeout

  lazy val boxOffice: ActorRef = createBoxOffice()

  def createEvent(event: String, numberOfTickets: Int): Future[EventResponse] =
    boxOffice.ask(CreateEvent(name = event, tickets = numberOfTickets)).mapTo[EventResponse]

  def getEvents: Future[Events] = boxOffice.ask(GetEvents).mapTo[Events]

  def getEvent(event: String): Future[Option[Event]] = boxOffice.ask(GetEvent(event)).mapTo[Option[Event]]

  def cancelEvent(event: String): Future[Option[Event]] = boxOffice.ask(CancelEvent(event)).mapTo[Option[Event]]

  def requestTickets(event: String, tickets: Int): Future[TicketSeller.Tickets] = boxOffice.ask(GetTickets(event, tickets)).mapTo[TicketSeller.Tickets]

}

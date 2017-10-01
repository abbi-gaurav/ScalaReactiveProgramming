package up.and.running

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern._
import akka.util.Timeout
import up.and.running.BoxOffice.{EventCreated, EventExists}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class RestApi(actorSystem: ActorSystem, timeout: Timeout) extends RestRoutes {
  implicit val requestTimeout: Timeout = timeout

  implicit def executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  override def createBoxOffice(): ActorRef = actorSystem.actorOf(BoxOffice.props, BoxOffice.name)
}

trait RestRoutes extends BoxOfficeApi with EventMarshalling {

  import akka.http.scaladsl.model.StatusCodes._

  def routes: Route = eventsRoute ~ eventRoute ~ ticketsRoute

  private def eventsRoute: Route = pathPrefix("events") {
    pathEndOrSingleSlash {
      get {
        onSuccess(getEvents)((events: BoxOffice.Events) => complete(OK, events))
      }
    }
  }

  private def eventRoute = pathPrefix("events" / Segment) { event =>
    pathEndOrSingleSlash {
      post {
        entity(as[EventDescription]) { ed =>
          onSuccess(createEvent(event, ed.tickets)) {
            case EventCreated(e) => complete(Created, e)
            case EventExists => complete(BadRequest, Error(s"$event event already exists"))
          }
        }
      } ~
        get {
          onSuccess(getEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        } ~
        delete {
          onSuccess(cancelEvent(event)) {
            _.fold(complete(NotFound))(e => complete(OK, e))
          }
        }
    }
  }

  private def ticketsRoute = pathPrefix("events" / Segment / "tickets") { event =>
    post {
      pathEndOrSingleSlash {
        entity(as[TicketRequest]) { request =>
          onSuccess(requestTickets(event, request.tickets)) { tickets =>
            if (tickets.entries.isEmpty) complete(NotFound) else complete(Created, tickets)
          }
        }
      }
    }
  }
}

trait BoxOfficeApi {

  import BoxOffice._

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
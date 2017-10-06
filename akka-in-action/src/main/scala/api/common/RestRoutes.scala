package api.common

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives.{as, complete, delete, entity, get, onSuccess, pathEndOrSingleSlash, pathPrefix, post, _}
import akka.http.scaladsl.server.Route
import api.common.actors.BoxOffice
import api.common.actors.BoxOffice.{EventCreated, EventExists}

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

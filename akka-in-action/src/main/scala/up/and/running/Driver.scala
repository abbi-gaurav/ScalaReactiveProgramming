package up.and.running

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import api.common.actors.BoxOffice
import api.common.{RequestTimeout, StartUp}
import com.typesafe.config.ConfigFactory

object Driver extends App with RequestTimeout with StartUp {
  private val config = ConfigFactory.load("up-and-running/up-and-running.conf")
  private implicit val system: ActorSystem = ActorSystem("up-and-running", config)
  private implicit val timeout: Timeout = requestTimeout(config)
  private val api: Route = new RestApi(system.dispatcher, timeout)(system.actorOf(BoxOffice.props, BoxOffice.name)).routes
  startUp(api)
}

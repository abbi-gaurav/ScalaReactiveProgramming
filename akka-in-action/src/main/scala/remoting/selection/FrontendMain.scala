package remoting

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import api.common.{RequestTimeout, StartUp}
import com.typesafe.config.ConfigFactory
import up.and.running.RestApi

object FrontendMain extends App with RequestTimeout with StartUp {
  private val config = ConfigFactory.load("remoting/frontend.conf")
  private implicit val system: ActorSystem = ActorSystem("frontend", config)
  private implicit val timeout: Timeout = requestTimeout(config)

  private def boxOfficeCreator: ActorRef = {
    def createPath: String = {
      val backendConfig = config.getConfig("backend")
      val host = backendConfig.getString("host")
      val port = backendConfig.getInt("port")
      val protocol = backendConfig.getString("protocol")
      val systemName = backendConfig.getString("system")
      val actorName = backendConfig.getString("actor")
      s"$protocol://$systemName@$host:$port/$actorName"
    }

    system.actorOf(Props(new RemoteLookupProxy(path = createPath)), "lookupBoxOffice")
  }

  private val api: Route = new RestApi(system.dispatcher, timeout)(boxOfficeCreator).routes

  startUp(api)
}

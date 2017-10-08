package remoting.deploy

import akka.actor.ActorSystem
import api.common.{RequestTimeout, StartUp}
import com.typesafe.config.ConfigFactory
import up.and.running.RestApi

object FrontendDeployWatchMain extends App with StartUp with RequestTimeout {
  private val config = ConfigFactory.load("remoting/frontend-remote-deployment.conf")
  private implicit val system: ActorSystem = ActorSystem("frontend", config)
  private val timeout = requestTimeout(config)
  private val api: RestApi = new RestApi(system.dispatcher, timeout)(
    system.actorOf(RemoteBoxOfficeForwarder.props(timeout), RemoteBoxOfficeForwarder.name)
  )

  startUp(api.routes)

}

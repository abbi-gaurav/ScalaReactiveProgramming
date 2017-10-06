package remoting

import akka.actor.ActorSystem
import akka.util.Timeout
import api.common.RequestTimeout
import api.common.actors.BoxOffice
import com.typesafe.config.ConfigFactory

object BackendMain extends App with RequestTimeout {
  private val config = ConfigFactory.load("remoting/backend.conf")
  private val actorSystem = ActorSystem("backend", config)
  private implicit val requestTimeout: Timeout = requestTimeout(config)
  actorSystem.actorOf(BoxOffice.props, BoxOffice.name)
}

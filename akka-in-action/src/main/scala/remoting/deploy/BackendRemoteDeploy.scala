package remoting.deploy

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory

object BackendRemoteDeploy extends App {
  val config = ConfigFactory.load("remoting/backend.conf")
  val system = ActorSystem("backend", config)
}

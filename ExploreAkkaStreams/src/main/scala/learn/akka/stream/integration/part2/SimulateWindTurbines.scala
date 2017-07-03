package learn.akka.stream.integration.part2

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.Config

/**
  * Created by gabbi on 25.06.17.
  */
object SimulateWindTurbines extends App {
  private implicit val system = ActorSystem()
  private implicit val actorMaterializer = ActorMaterializer()

  for (_ <- 1 to 1) {
    val id = java.util.UUID.randomUUID().toString
    //TODO: Config.endpoint
    system.actorOf(WindTurbineSimulator.props(id, "ws://echo.websocket.org"), id)
  }
}

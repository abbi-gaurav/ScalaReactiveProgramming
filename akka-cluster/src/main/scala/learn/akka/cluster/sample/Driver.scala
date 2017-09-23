package learn.akka.cluster.sample

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.Config

object Driver extends App {
  private val config: Config = ClusterConfig.globalConfig

  private val app = config.getString("args.app-name")
  private val system = ActorSystem(name = app, config = config)

  private val role: Role = Role(config.getStringList("akka.cluster.roles").get(0))

  role match {
    case Producer => system.actorOf(Props[ProducerActor], role.getDomainString)
    case Consumer => system.actorOf(Props[ConsumerActor], role.getDomainString)
    case Seed =>
  }

}

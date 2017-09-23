package learn.akka.cluster.sample

import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}

object Driver extends App {
  private val role: Role = args.headOption map Role.apply getOrElse Seed
  private val roleString = role.toString.toLowerCase
  private val config: Config = ConfigFactory
    .parseString(s"""akka.cluster.roles = ["$roleString"]""")
    .withFallback(ConfigFactory.load())

  private val app = config.getString("args.app-name")
  private val system = ActorSystem(name = app, config = config)

  role match {
    case Producer => system.actorOf(Props[ProducerActor], roleString)
    case Consumer => system.actorOf(Props[ConsumerActor], roleString)
    case Seed =>
  }

}

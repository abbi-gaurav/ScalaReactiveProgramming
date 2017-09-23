package learn.akka.cluster.sample

import java.lang

import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import learn.akka.cluster.sample.ClusterConfig.split

object ClusterConfig {
  lazy val globalConfig: Config = ClusterConfig(ConfigFactory.load())
    .configureRoles
    .configureSeeds
    .config

  private def split(string: String, regex: String = ",")(mapper: String => String): java.lang.Iterable[String] = {
    import collection.JavaConverters._
    asJavaIterable(string.split(regex) map mapper)
  }
}

case class ClusterConfig(config: Config) {

  private def configureRoles: ClusterConfig = {
    val argRoles: lang.Iterable[String] = split(config.getString("args.roles"))(identity)
    ClusterConfig(
      config.withValue("akka.cluster.roles", ConfigValueFactory.fromIterable(argRoles))
    )
  }

  private def configureSeeds: ClusterConfig = {
    val seeds: lang.Iterable[String] = split(config.getString("args.seed-hosts")) { x =>
      s"""akka.tcp://${config.getString("args.app-name")}@$x:2551"""
    }
    ClusterConfig(
      config.withValue("akka.cluster.seed-nodes", ConfigValueFactory.fromIterable(seeds))
    )
  }
}

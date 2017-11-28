package gabbi.learn.emojitrends.guice

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

class AkkaModule(config: Config) extends AbstractModule with ScalaModule {
  private lazy val system = ActorSystem("emoji-trends")

  override def configure(): Unit = bind[ActorSystem].toInstance(system)

  @Singleton
  @Provides
  def provideExecutionContext(implicit system: ActorSystem): ExecutionContext = system.dispatcher

  @Singleton
  @Provides
  def provideMaterializer(implicit system: ActorSystem): Materializer = ActorMaterializer()
}

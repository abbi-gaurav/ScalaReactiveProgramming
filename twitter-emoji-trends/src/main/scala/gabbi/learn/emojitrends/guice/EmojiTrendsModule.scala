package gabbi.learn.emojitrends.guice

import javax.inject.Singleton

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.stream.Materializer
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.ConfigFactory
import gabbi.learn.emojitrends.persistence.RedisEmojiCounterDao
import gabbi.learn.emojitrends.stream.EmojiCounter
import net.codingwell.scalaguice.ScalaModule

import scala.concurrent.ExecutionContext

class EmojiTrendsModule extends AbstractModule with ScalaModule {
  private lazy val config = ConfigFactory.load

  override def configure(): Unit = {
    install(new TwitterModule(config.getConfig("twitter")))
    install(new AkkaModule(config.getConfig("akka")))
    install(new RedisModule(config.getConfig("redis")))
  }

  @Provides
  @Singleton
  def provideEmojiCounter(dao: RedisEmojiCounterDao)
                         (implicit system: ActorSystem,
                          executionContext: ExecutionContext,
                          materializer: Materializer): EmojiCounter = {
    implicit val loggingAdapter: LoggingAdapter = Logging(system, classOf[EmojiCounter])
    val configuration = EmojiCounter.Configuration(config.getInt("emojiCounter.bufferSize"))
    new EmojiCounter(dao, configuration)
  }
}

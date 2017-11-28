package gabbi.learn.emojitrends.guice

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import com.redis.RedisClientPool
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule

class RedisModule(redisConfig: Config) extends AbstractModule with ScalaModule {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def provideRedisClientPool(): RedisClientPool = new RedisClientPool(
    host = redisConfig.getString("host"),
    port = redisConfig.getInt("port"),
    database = redisConfig.getInt("database")
  )
}

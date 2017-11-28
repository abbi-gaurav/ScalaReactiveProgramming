package gabbi.learn.emojitrends.guice

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.Config
import net.codingwell.scalaguice.ScalaModule
import twitter4j.conf.{Configuration, ConfigurationBuilder}
import twitter4j.{TwitterStream, TwitterStreamFactory}

class TwitterModule(twitterConfig: Config) extends AbstractModule with ScalaModule {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def provideTwitterStream(): TwitterStream = {
    val oAuthConfig = twitterConfig.getConfig("oauth")

    val configuration: Configuration = new ConfigurationBuilder()
      .setOAuthConsumerKey(oAuthConfig.getString("consumerKey"))
      .setOAuthConsumerSecret(oAuthConfig.getString("consumerSecret"))
      .setOAuthAccessToken(oAuthConfig.getString("token"))
      .setOAuthAccessTokenSecret(oAuthConfig.getString("tokenSecret"))
      .build

    new TwitterStreamFactory(configuration).getInstance()
  }
}

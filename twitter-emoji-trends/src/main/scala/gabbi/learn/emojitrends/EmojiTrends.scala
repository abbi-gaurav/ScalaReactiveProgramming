package gabbi.learn.emojitrends

import com.google.inject.Guice
import com.typesafe.scalalogging.LazyLogging
import gabbi.learn.emojitrends.guice.{AkkaModule, EmojiTrendsModule}
import gabbi.learn.emojitrends.stream.EmojiCounter
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import twitter4j.TwitterStream

object EmojiTrends extends App with LazyLogging {
  private val emojiTrendsModule = new EmojiTrendsModule
  private val injector = new ScalaInjector(Guice.createInjector(emojiTrendsModule))

  private val twitterStream = injector.instance[TwitterStream]
  private val counter = injector.instance[EmojiCounter]
  twitterStream.addListener(counter)

  twitterStream.sample()
}

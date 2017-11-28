package gabbi.learn.emojitrends.stream

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.scaladsl._
import akka.stream.{Materializer, OverflowStrategy}
import com.vdurmont.emoji.EmojiParser
import gabbi.learn.emojitrends.persistence.RedisEmojiCounterDao
import gabbi.learn.emojitrends.stream.EmojiCounter.Configuration
import twitter4j.{Status, StatusAdapter}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.collection.JavaConverters._

class EmojiCounter(
                    dao: RedisEmojiCounterDao,
                    configuration: Configuration
                  )
                  (implicit val materializer: Materializer,
                   executionContext: ExecutionContext,
                   loggingAdapter: LoggingAdapter
                  ) extends StatusAdapter {
  private implicit val system: ActorSystem = ActorSystem("emoji-trends")

  private val overflowStrategy: OverflowStrategy = OverflowStrategy.backpressure
  private val bufferSize = 1000
  private val statusSource: Source[Status, SourceQueueWithComplete[Status]] = Source.queue[Status](bufferSize = bufferSize, overflowStrategy = overflowStrategy)

  private val extractEmojis = Flow[Status]
    .map(status => EmojiParser.extractEmojis(status.getText).asScala)
    .async
    .filter(emojis => emojis.nonEmpty)
    .log("EMOJIS", emojis => emojis.reduce(_ + _))

  private val incrementTweets = Flow[Seq[String]]
    .map { emojis =>
      dao.incrementTweets
      emojis
    }
    .async

  private val incrementsEmoji = Flow[Seq[String]]
    .mapConcat(emojis => emojis.distinct.toList)
    .async
    .map { emoji =>
      dao.incrementEmoji(emoji)
      emoji
    }
    .log("IDF", { emoji =>
      val idf: Long = math.round(
        math.log1p(dao.tweets.toDouble / dao.occurrences(emoji))
      )
      s"""$emoji ${"*" * idf.toInt}"""
    })

  private val graph: RunnableGraph[SourceQueueWithComplete[Status]] = statusSource
    .via(extractEmojis)
    .via(incrementTweets)
    .via(incrementsEmoji)
    .to(Sink.ignore)

  private val queue: SourceQueueWithComplete[Status] = graph.run()

  override def onStatus(status: Status): Unit = Await.result(queue.offer(status), Duration.Inf)
}

object EmojiCounter {

  case class Configuration(bufferSize: Int)

}

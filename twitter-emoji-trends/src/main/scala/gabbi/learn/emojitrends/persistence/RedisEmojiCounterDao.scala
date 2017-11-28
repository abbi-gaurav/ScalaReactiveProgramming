package gabbi.learn.emojitrends.persistence

import javax.inject.Inject

import com.redis.RedisClientPool

class RedisEmojiCounterDao @Inject()(pool: RedisClientPool) {
  def incrementTweets: Option[Long] = pool.withClient(_.incr("tweets"))

  def incrementEmoji(emoji: String): Option[Long] = pool.withClient(_.incr(emojiKey(emoji)))

  def tweets: Int = pool.withClient(_.get("tweets")) map (_.toInt) getOrElse 0

  def occurrences(emoji: String): Int = pool.withClient(_.get(emojiKey(emoji))) map (_.toInt) getOrElse 0

  private def emojiKey(emoji: String): String = s"emoji:$emoji"
}

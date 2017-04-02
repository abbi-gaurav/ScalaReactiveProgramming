package learn.akka.stream.motivatingExample

/**
  * Created by gabbi on 02.04.17.
  */
class Database() {
  def bulkInsertAsync(messages: Seq[String]) = println(s"inserting bulk ${messages.size} messages")

  def insertAsync(message: String): Unit = println(s"persisting message $message")

}

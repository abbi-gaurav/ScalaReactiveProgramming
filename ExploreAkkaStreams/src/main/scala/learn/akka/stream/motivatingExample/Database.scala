package learn.akka.stream.motivatingExample

/**
  * Created by gabbi on 02.04.17.
  */
class Database() {
  def insertAsync(message: String): Unit = println(s"persisting message $message")

}

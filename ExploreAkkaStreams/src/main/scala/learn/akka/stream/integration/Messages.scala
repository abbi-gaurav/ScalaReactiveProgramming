package learn.akka.stream.integration

import akka.http.scaladsl.model.ws.TextMessage

/**
  * Created by gabbi on 23.04.17.
  */
object Messages {
  def ack(lastMessage: String): TextMessage = TextMessage(s"ack $lastMessage")

  def parse(strings: Seq[String]): Measurements = Measurements(strings.size, strings.last)
}

case class Measurements(sum: Long, last: String)

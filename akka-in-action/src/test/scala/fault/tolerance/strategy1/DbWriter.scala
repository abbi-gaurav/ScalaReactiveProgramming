package fault.tolerance.strategy1

import akka.actor.{Actor, Props}
import fault.tolerance.common.DbCon

object DbWriter {
  def props(databaseUrl: String) = Props(new DbWriter(databaseUrl))

  def name(databaseUrl: String) = s"""db-writer-${databaseUrl.split("/").last}"""

  case class Line(time: Long, message: String, messageType: String)

}

class DbWriter(databaseUrl: String) extends Actor {
  val conn = new DbCon(databaseUrl)

  import DbWriter._

  override def receive: Receive = {
    case Line(time, message, messageType) => conn.write(
      Map(
        'time -> time,
        'message -> message,
        'messageType -> messageType
      )
    )
  }

  override def postStop(): Unit = conn.close()
}
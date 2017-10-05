package fault.tolerance.strategy1

import java.io.File
import java.util.UUID

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, Terminated}
import fault.tolerance.common.{DBNodeDownException, DBbrokenConnectionException, LogParsing}

class LogProcessor(databaseUrls: Vector[String]) extends Actor with ActorLogging with LogParsing {
  require(databaseUrls.nonEmpty)

  private val initialDatabaseUrl: String = databaseUrls.head
  private var alternateDatabaseUrls: Vector[String] = databaseUrls.tail

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case _: DBNodeDownException => Stop
    case _: DBbrokenConnectionException => Restart
  }

  private var dbWriter: ActorRef = createDBWriter(initialDatabaseUrl)

  import fault.tolerance.strategy1.LogProcessor._

  override def receive: Receive = {
    case LogFile(file) => parse(file) foreach (dbWriter ! _)
    case Terminated(_) =>
      if (alternateDatabaseUrls.nonEmpty) {
        val newDatabaseUrl = alternateDatabaseUrls.head
        alternateDatabaseUrls = alternateDatabaseUrls.tail
        dbWriter = createDBWriter(newDatabaseUrl)
      } else {
        log.error("All db nodes are broken. Stopping !!!")
        self ! PoisonPill
      }
  }

  private def createDBWriter(databaseUrl: String): ActorRef = {
    val actorRef: ActorRef = context.actorOf(DbWriter.props(databaseUrl), DbWriter.name(databaseUrl))
    context.watch(actorRef)
    actorRef
  }

}

object LogProcessor {
  def props(databaseUrls: Vector[String]): Props = Props(new LogProcessor(databaseUrls))

  def name: String = s"log-processor-${UUID.randomUUID.toString}"

  case class LogFile(file: File)

}

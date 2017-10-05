package fault.tolerance.strategy1

import java.io.File

import akka.actor.SupervisorStrategy.Resume
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, PoisonPill, Terminated}
import fault.tolerance.common.{CorruptedFileException, FileWatchingAbilities}
import fault.tolerance.strategy1.FileWatcher.{NewFile, SourceAbandoned}

class FileWatcher(source: String, databaseUrls: Vector[String])
  extends Actor with ActorLogging with FileWatchingAbilities {
  register(source)

  override def supervisorStrategy: OneForOneStrategy = OneForOneStrategy() {
    case _: CorruptedFileException => Resume
  }

  private val logProcessor = context.actorOf(LogProcessor.props(databaseUrls), LogProcessor.name)

  context.watch(logProcessor)

  override def receive: Receive = {
    case NewFile(file, _) =>
      logProcessor ! LogProcessor.LogFile(file)
    case SourceAbandoned(uri) if uri == source =>
      log.info(s"Source $source abandoned, stopping watcher")
      self ! PoisonPill
    case Terminated(`logProcessor`) =>
      log.info(s"Log processor terminated, stopping watcher")
      self ! PoisonPill
  }
}

object FileWatcher {

  case class NewFile(file: File, timeAdded: Long)

  case class SourceAbandoned(uri: String)

}

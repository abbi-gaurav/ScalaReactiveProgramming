package fault.tolerance.strategy1

import akka.actor.SupervisorStrategy.Stop
import akka.actor.{Actor, ActorLogging, ActorRef, AllForOneStrategy, Props, Terminated}
import fault.tolerance.common.DiskError

class LogProcessingSupervisor(sources: Vector[String], databaseUrls: Vector[String]) extends Actor with ActorLogging {

  private var fileWatchers: Vector[ActorRef] = sources map { source =>
    val fileWatcher = context.actorOf(Props(new FileWatcher(source, databaseUrls)))
    context.watch(fileWatcher)
    fileWatcher
  }

  override def supervisorStrategy: AllForOneStrategy = AllForOneStrategy() {
    case _: DiskError => Stop
  }

  override def receive: Receive = {
    case Terminated(fileWatcher) =>
      fileWatchers = fileWatchers filterNot (_ != fileWatcher)
      if (fileWatchers.isEmpty) {
        log.info(s"Shutting down system as all watchers are down")
        context.system.terminate()
      }
  }
}

object LogProcessingSupervisor{
  def props(sources:Vector[String], databaseUrls:Vector[String]) = Props(new LogProcessingSupervisor(sources, databaseUrls))
  def name = "file-watch-supervisor"
}
package w5.urlFetcher

import akka.actor._
import w5.urlFetcher.Receptionist.{Failed, Get, Job, Result}

/**
 * Created by gabbi on 07/06/15.
 */
class Receptionist extends Actor {
  var reqNo = 0


  override def supervisorStrategy: SupervisorStrategy = SupervisorStrategy.stoppingStrategy

  override def receive: Receive = waiting

  def waiting: Receive = {
    case Get(url) => context.become(runNext(Vector(Job(sender(), url))))
  }

  def enqueueJob(queue: Vector[Job], job: Job): Actor.Receive = {
    if (queue.size > 3) {
      job.client ! Receptionist.Failed(job.url)
      running(queue)
    } else {
      running(queue :+ job)
    }
  }

  def running(queue: Vector[Job]): Receive = {
    case Controller.Result(links) =>
      val job = queue.head
      job.client ! Result(job.url, links)
      //here sender is the child actor as per the contract
      context.stop(context.unwatch(sender()))
      context.become(runNext(queue.tail))
    case Get(url) => context.become(enqueueJob(queue, Job(sender(), url)))
    case Terminated(_) =>
      val job = queue.head
      job.client ! Failed(job.url)
      context.become(runNext(queue.tail))
  }

  def runNext(queue: Vector[Job]): Receive = {
    reqNo += 1

    //effect of DeathPactException
//    if(reqNo == 3) context.stop(self)

    if (queue.isEmpty) waiting
    else {
      val controller = context.actorOf(controllerProps, s"c$reqNo")
      context.watch(controller)
      controller ! Controller.Check(queue.head.url, 2)
      running(queue)
    }
  }

  def controllerProps: Props = {
    Props[Controller]
  }
}

object Receptionist {

  case class Job(client: ActorRef, url: String)

  case class Get(url: String)

  case class Result(url: String, links: Set[String])

  case class Failed(url: String)

}

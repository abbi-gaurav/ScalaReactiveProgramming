package structure

import java.text.SimpleDateFormat
import java.util.Date

import akka.actor.{Actor, ActorRef}

import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.FiniteDuration

case class PhotoMessage(id: String,
                        photo: String,
                        creationTime: Option[Date] = None,
                        speed: Option[Int] = None
                       )

object ImageProcessing {
  val dateFormat = new SimpleDateFormat("ddMMyyyy HH:mm:ss.SSS")

  def getSpeed(image: String): Option[Int] = {
    val attributes = image.split('|')
    if (attributes.length == 3)
      Some(attributes(1).toInt)
    else
      None
  }

  def getTime(image: String): Option[Date] = {
    val attributes = image.split('|')
    if (attributes.length == 3)
      Some(dateFormat.parse(attributes(0)))
    else
      None
  }

  def getLicense(image: String): Option[String] = {
    val attributes = image.split('|')
    if (attributes.length == 3)
      Some(attributes(2))
    else
      None
  }

  def createPhotoString(date: Date, speed: Int): String = {
    createPhotoString(date, speed, " ")
  }

  def createPhotoString(date: Date,
                        speed: Int,
                        license: String): String = {
    "%s|%s|%s".format(dateFormat.format(date), speed, license)
  }
}

class GetSpeed(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: PhotoMessage => pipe ! msg.copy(speed = ImageProcessing.getSpeed(msg.photo))
  }
}

class GetTime(pipe:ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: PhotoMessage => pipe ! msg.copy(creationTime = ImageProcessing.getTime(msg.photo))
  }
}

class RecipientList(recipients: List[ActorRef]) extends Actor {
  override def receive: Receive = {
    case msg: AnyRef => recipients foreach (_ ! msg)
  }
}

case class TimeoutMessage(photoMessage: PhotoMessage)

class Aggregator(timeout: FiniteDuration, pipe: ActorRef) extends Actor {
  private val messages = new ListBuffer[PhotoMessage]
  private implicit val ec = context.dispatcher


  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    messages.foreach(self ! _)
    messages.clear()
  }

  override def receive: Receive = {
    case photoMessage: PhotoMessage =>
      messages.find(_.id == photoMessage.id) match {
        case Some(alreadyRcvMsg) =>
          val newCombinedMsg = PhotoMessage(
            id = photoMessage.id,
            photo = photoMessage.photo,
            creationTime = photoMessage.creationTime.orElse(alreadyRcvMsg.creationTime),
            photoMessage.speed.orElse(alreadyRcvMsg.speed)
          )
          pipe ! newCombinedMsg
          messages -= alreadyRcvMsg

        case None =>
          messages += photoMessage
          context.system.scheduler.scheduleOnce(
            timeout,
            self,
            TimeoutMessage(photoMessage)
          )
      }
    case TimeoutMessage(photoMessage) =>
      messages.find(_.id == photoMessage) match {
        case None =>
        case Some(notAggregatedMsg) =>
          pipe ! notAggregatedMsg
          messages -= notAggregatedMsg
      }
    case ex: Exception => throw ex
  }
}

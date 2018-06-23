package akka.cookbook.streams.error

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{ActorAttributes, ActorMaterializer, ActorMaterializerSettings, Supervision}

object HandlingErrorsApplication extends App {
  implicit val actorSystem: ActorSystem = ActorSystem()

  val streamDecider: Supervision.Decider = {
    case _: IndexOutOfBoundsException =>
      println(s"dropping element because of IOBE, resuming")
      Supervision.Resume
    case _ => Supervision.Stop
  }

  val flowDecider: Supervision.Decider = {
    case _: IllegalArgumentException =>
      println(s"dropping element because of IAE, restarting")
      Supervision.Restart

    case _ => Supervision.Stop
  }

  val actorMaterializerSettings: ActorMaterializerSettings = ActorMaterializerSettings(actorSystem)
    .withSupervisionStrategy(streamDecider)

  implicit val actorMaterializer: ActorMaterializer = ActorMaterializer(actorMaterializerSettings)

  val words: List[String] = "Handling errors in Akka Streams".split(" ").toList ++ List("")

  val flow: Flow[String, String, NotUsed] = Flow[String].map(word => {
    if (word.isEmpty) throw new IllegalArgumentException("No empty words")
    word
  }).withAttributes(ActorAttributes.supervisionStrategy(flowDecider))

  Source(words)
    .via(flow)
    .map(array => array(2))
    .to(Sink.foreach(println))
    .run()
}

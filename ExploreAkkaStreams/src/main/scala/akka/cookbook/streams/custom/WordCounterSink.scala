package akka.cookbook.streams.custom

import akka.stream.{Attributes, Inlet, SinkShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, TimerGraphStageLogic}

import scala.concurrent.duration._

class WordCounterSink extends GraphStage[SinkShape[String]] {
  private val inlet: Inlet[String] = Inlet("WordCountSink")

  override def shape: SinkShape[String] = SinkShape(inlet)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new TimerGraphStageLogic(shape) {
    var counts: Map[String, Int] = Map.empty[String, Int].withDefaultValue(0)

    override def preStart(): Unit = {
      schedulePeriodically(None, 5 seconds)
      pull(inlet)
    }

    setHandler(inlet, new InHandler {
      override def onPush(): Unit = {
        val word = grab(inlet)
        counts += word -> (counts(word) + 1)
        pull(inlet)
      }
    })

    override protected def onTimer(timerKey: Any): Unit = {
      println(s"At time ${System.currentTimeMillis()} count map is $counts")
    }
  }

}

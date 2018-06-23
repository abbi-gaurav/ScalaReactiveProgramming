package akka.cookbook.streams.custom

import akka.stream.{Attributes, Outlet, SourceShape}
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}

class HelloAkkaStreamsSource extends GraphStage[SourceShape[String]] {
  private val outlet: Outlet[String] = Outlet("SystemInputSource")

  override def shape: SourceShape[String] = SourceShape(out = outlet)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape = shape) {
    setHandler(out = outlet, new OutHandler {
      override def onPull(): Unit = {
        val line = "Hello custom source in Akka Streams!!"
        push(outlet, line)
      }
    })
  }
}

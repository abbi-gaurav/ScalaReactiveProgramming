package learn.akka.stream.gs

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream._

/**
  * Created by gabbi on 23.07.17.
  */
class MyFilterGraphStage[T](predicate: T => Boolean) extends GraphStage[FlowShape[T, T]] {

  private val inlet: Inlet[T] = Inlet[T]("MyFilter.in")
  private val outlet = Outlet[T]("MyFilter.out")

  override def shape: FlowShape[T, T] = FlowShape(in = inlet, out = outlet)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape = shape) {
    setHandler(in = inlet, new InHandler {
      override def onPush(): Unit = {
        val element: T = grab(inlet)
        if (predicate(element)) push(outlet, element) else pull(in = inlet)
      }
    })
    setHandler(out = outlet, new OutHandler {
      override def onPull(): Unit = pull(in = inlet)
    })
  }
}

object TestGS extends App{
  private implicit val system = ActorSystem()
  private implicit val mat = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 10)
  val filterGraphStage: MyFilterGraphStage[Int] = new MyFilterGraphStage[Int](_ % 2 == 0)
  val flow = Flow.fromGraph(filterGraphStage)
  source.via(flow).runForeach(println)
}
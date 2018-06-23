package akka.cookbook.streams.pipeliningandparallel

import akka.stream.scaladsl._

object SynchronousApp extends PipeliningParallelizing {
  runGraph(
    Flow[Wash]
      .via(washStage)
      .via(dryStage)
  )
}

object AsyncApp extends PipeliningParallelizing {
  runGraph(
    Flow[Wash]
      .via(washStage.async)
      .via(dryStage.async)
  )
}

object ParallelApp extends PipeliningParallelizing {
  runGraph(
    Flow[Wash]
      .via(parallelStage)
  )
}
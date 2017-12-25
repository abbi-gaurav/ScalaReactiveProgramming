package explore.grpc

import io.grpc.{ServerBuilder, ServerServiceDefinition}

trait GrpcServer {
  def runServer(ssd: ServerServiceDefinition): Unit = {
    val server = ServerBuilder
      .forPort(25001)
      .addService(ssd)
      .build
      .start

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = server.shutdown()
    })

    server.awaitTermination()
  }
}

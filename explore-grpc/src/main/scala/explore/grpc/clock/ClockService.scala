package explore.grpc.clock

import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicInteger

import clock.ClockGrpc.Clock
import clock.{ClockGrpc, TimeRequest, TimeResponse}
import explore.grpc.GrpcServer
import io.grpc.stub.StreamObserver

import scala.concurrent.ExecutionContext

class ClockService extends Clock {
  override def getTime(request: TimeRequest, responseObserver: StreamObserver[TimeResponse]): Unit = {
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val tick = new Runnable {
      val counter = new AtomicInteger(10)

      override def run(): Unit = {
        if (counter.getAndDecrement() >= 0) {
          val timeResponse = TimeResponse(System.currentTimeMillis())
          responseObserver.onNext(timeResponse)
        } else {
          scheduler.shutdown()
          responseObserver.onCompleted()
        }
      }
    }
    scheduler.scheduleAtFixedRate(tick, 1, 10001, TimeUnit.MILLISECONDS)
  }
}

object ClockServer extends GrpcServer {
  def main(args: Array[String]): Unit = {
    val ssd = ClockGrpc.bindService(new ClockService(), ExecutionContext.global)
    runServer(ssd)
  }
}

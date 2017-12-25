package explore.grpc.clock

import java.util.concurrent.CountDownLatch

import clock.{ClockGrpc, TimeRequest, TimeResponse}
import io.grpc.ManagedChannelBuilder
import io.grpc.stub.StreamObserver

object ClockClient {
  def main(args: Array[String]): Unit = {
    val channel = ManagedChannelBuilder.forAddress("localhost", 25001).usePlaintext(true).build()
    val client: ClockGrpc.ClockStub = ClockGrpc.stub(channel)

    val cl = new CountDownLatch(1)

    val observer = new StreamObserver[TimeResponse] {

      override def onError(t: Throwable): Unit = t.printStackTrace()

      override def onCompleted(): Unit = {
        println("completed")
        cl.countDown()
      }

      override def onNext(value: TimeResponse): Unit = print(s"received $value")
    }

    client.getTime(TimeRequest.defaultInstance, observer)

    cl.await()
  }
}

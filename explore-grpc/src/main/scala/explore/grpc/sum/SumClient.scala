package explore.grpc.sum

import io.grpc.ManagedChannelBuilder
import sum.{SumGrpc, SumRequest}

object SumClient {
  def main(args: Array[String]): Unit = {
    val channel = ManagedChannelBuilder.forAddress("localhost", 25001).usePlaintext(true).build()
    val request = SumRequest(a = 3, b = 4)

    val blockingClient = SumGrpc.blockingStub(channel = channel)
    val response = blockingClient.calcSum(request)

    println(response)
  }
}

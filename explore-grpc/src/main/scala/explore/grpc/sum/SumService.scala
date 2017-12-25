package explore.grpc.sum

import explore.grpc.GrpcServer
import sum.{SumGrpc, SumRequest, SumResponse}

import scala.concurrent.{ExecutionContext, Future}

class SumService extends SumGrpc.Sum {
  override def calcSum(request: SumRequest): Future[SumResponse] = {
    val sumResponse = SumResponse(request.a + request.b)
    Future.successful(sumResponse)
  }
}

object SumServer extends GrpcServer {
  def main(args: Array[String]): Unit = {
    val ssd = SumGrpc.bindService(new SumService(), ExecutionContext.global)

    runServer(ssd)
  }
}

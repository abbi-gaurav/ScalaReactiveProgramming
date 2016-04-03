package w3.futures

import scala.concurrent.Future

/**
 * Created by gabbi on 21/05/15.
 */
object SocketRun extends App {
  import scala.concurrent.ExecutionContext.Implicits.global
  private def run(socket: Socket): Unit = {
    val bytes: Future[Array[Byte]] = socket.readFromMemory()
    val result: Future[Array[Byte]] = bytes flatMap { p => trySend(socket, p) }
  }

  private def trySend(socket: Socket, p: Array[Byte]): Future[Array[Byte]] = {
    socket.send2India(p) recoverWith{
      case error:Error => socket.send2India(p) recover{
        case secondErr:Error => secondErr.getMessage.getBytes
      }
    }
  }

  private def runWithComp(socket: Socket):Future[Array[Byte]] = {
    for{
      bytes <- socket.readFromMemory()
      confirmation <- socket.send2India(bytes)
    }yield confirmation
  }
}

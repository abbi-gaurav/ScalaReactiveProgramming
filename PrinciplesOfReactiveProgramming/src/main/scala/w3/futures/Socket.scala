package w3.futures

import scala.concurrent.Future

/**
 * Created by gabbi on 14/05/15.
 */
trait Socket {
  def readFromMemory(): Future[Array[Byte]]

  def send2India(packet: Array[Byte]): Future[Array[Byte]]
}

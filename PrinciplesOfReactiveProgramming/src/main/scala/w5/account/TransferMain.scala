package w5.account

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive

/**
 * Created by gabbi on 07/06/15.
 */
class TransferMain extends Actor {
  private val accountA = context.actorOf(Props[BankAccount], "a")
  private val accountB = context.actorOf(Props[BankAccount], "b")
  accountA ! BankAccount.Deposit(100)

  def transfer(amount: BigInt): Unit = {
    val transaction = context.actorOf(Props[WireTransfer], "xt")
    transaction ! WireTransfer.Transfer(accountA, accountB, amount)
    context.become(LoggingReceive {
      case WireTransfer.Done =>
        println("success")
        context.stop(self)
      case WireTransfer.Failed =>
        println("failed")
        context.stop(self)
    })
  }

  override def receive = LoggingReceive{
    case BankAccount.Done => transfer(150)
  }
}

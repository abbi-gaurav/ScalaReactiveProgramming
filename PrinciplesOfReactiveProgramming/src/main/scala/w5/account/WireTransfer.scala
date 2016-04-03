package w5.account

import akka.actor.{Actor, ActorRef}

/**
 * Created by gabbi on 07/06/15.
 */
class WireTransfer extends Actor{

  def awaitDeposit(client: ActorRef): Actor.Receive = {
    case BankAccount.Done =>
      client ! WireTransfer.Done
      context.stop(self)
  }

  def awaitWithdraw(to: ActorRef, amount: BigInt, client: ActorRef): Receive = {
    case BankAccount.Done =>
      to ! BankAccount.Deposit(amount)
      context.become(awaitDeposit(client))
    case BankAccount.Failed =>
      client ! WireTransfer.Failed
      context.stop(self)
  }

  override def receive: Receive = {
    case WireTransfer.Transfer(from, to, amount) =>
      from ! BankAccount.Withdraw(amount)
      context.become(awaitWithdraw(to, amount, sender()))
  }
}

object WireTransfer{
  case class Transfer(from:ActorRef, to:ActorRef, amount:BigInt)
  case object Done
  case object Failed
}

package w2.observer

/**
 * Created by gabbi on 07/05/15.
 */
class Consolidator(observed: List[BankAccount]) extends Subscriber {
  private var total: Int = _

  observed.foreach(_.subscribe(this))

  compute()

  private def compute() = {
    total = observed.foldLeft(0) {
      case (acc, bankAccount) => acc + bankAccount.currentBalance
    }
  }

  override def handle(p: Publisher): Unit = compute()

  def totalBalance: Int = total
}

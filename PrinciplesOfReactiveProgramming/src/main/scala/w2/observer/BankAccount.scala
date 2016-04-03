package w2.observer

/**
 * Created by gabbi on 07/05/15.
 */
class BankAccount extends Publisher{
  private var balance:Int = 0

  def deposit(amount:Int):Unit = {
    if(amount > 0){
      balance += amount
      publish()
    }
  }

  def withdraw(amount:Int):Unit = {
    if(amount > 0 && balance >= amount){
      balance -= amount
      publish()
    }else throw new Error("insufficient balance")
  }

  def currentBalance:Int = balance

}

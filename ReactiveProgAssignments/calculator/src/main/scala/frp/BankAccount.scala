package frp

import calculator.Var

/**
 * Created by gabbi on 07/05/15.
 */
class BankAccount{
  val balance = Var(0)

  def deposit(amount:Int):Unit = {
    if(amount > 0){
      val b: Int = balance()
      balance() = b + amount
    }
  }

  def withdraw(amount:Int):Unit = {
    if(amount > 0 && balance() >= amount){
      val b: Int = balance()
      balance() = b - amount
    }else throw new Error("insufficient balance")
  }
}

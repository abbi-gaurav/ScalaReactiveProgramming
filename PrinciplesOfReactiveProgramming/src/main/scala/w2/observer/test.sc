import w2.observer.{Consolidator, BankAccount}

val a = new BankAccount
val b = new BankAccount
val c = new Consolidator(List(a,b))
c.totalBalance

a.deposit(10)
b.deposit(200)
c.totalBalance
a.withdraw(4)
c.totalBalance
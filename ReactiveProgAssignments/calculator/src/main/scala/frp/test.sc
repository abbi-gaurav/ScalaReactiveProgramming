import calculator.Signal
import frp.BankAccount
def consolidated(accts:List[BankAccount]) = Signal(accts.map(_.balance()).sum)

val a = new BankAccount
val b = new BankAccount
val c = consolidated(List(a,b))
c()

a deposit 20
b deposit 100
c()

val xchange = Signal(65.0)
val inRupees = Signal(xchange() * c())
inRupees()

b withdraw 10
inRupees()

package w2.des

/**
 * Created by gabbi on 03/05/15.
 */
abstract class Gates extends Simulation {
  def InverterDelay: Int

  def AndDelay: Int

  def OrDelay: Int

  class Wire {
    private var sigVal: Boolean = false
    private var actions: List[Action] = List()

    def getSignal = sigVal

    def setSignal(signal: Boolean): Unit = {
      if (sigVal != signal) {
        sigVal = signal
        for (a <- actions) a()
      }
    }

    def addAction(a: Action): Unit = {
      actions = a :: actions
      a()
    }
  }

  def inverter(in: Wire, out: Wire): Unit = {
    def invertAction(): Unit = {
      val inputSignal = in.getSignal
      afterDelay(InverterDelay) {
        out setSignal !inputSignal
      }
    }
    in addAction invertAction
  }

  def andGate(in1: Wire, in2: Wire, out: Wire): Unit = {
    def andAction(): Unit = {
      val a1 = in1.getSignal
      val a2 = in2.getSignal
      afterDelay(AndDelay) {
        out setSignal a1 & a2
      }
    }
    in1 addAction andAction
    in2 addAction andAction
  }

  def orGate(in1: Wire, in2: Wire, out: Wire): Unit = {
    def orAction(): Unit = {
      val o1 = in1.getSignal
      val o2 = in2.getSignal
      afterDelay(OrDelay) {
        out setSignal o1 | o2
      }
    }
    in1 addAction orAction
    in2 addAction orAction
  }

  def probe(name: String, wire: Wire): Unit = {
    def probeAction(): Unit = {
      println(s"$name $currentTime value = ${wire.getSignal}")
    }
    wire addAction probeAction
  }
}

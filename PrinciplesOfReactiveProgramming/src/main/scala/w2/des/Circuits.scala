package w2.des

/**
 * Created by gabbi on 03/05/15.
 */
abstract class Circuits extends Gates{
  def halfAdder(a:Wire, b:Wire, sum:Wire, carry:Wire):Unit = {
    val d = new Wire
    val e = new Wire

    andGate(a, b, carry)
    orGate(a,b,d)
    inverter(carry, e)
    andGate(d,e,sum)
  }

  def fullAdder(a:Wire, b:Wire, cInp:Wire, sum:Wire, cOut:Wire):Unit = {
    val s, c1, c2 = new Wire
    halfAdder(b, cInp, s, c1)
    halfAdder(a,s, sum, c2)
    orGate(c2,c1, cOut)
  }
}

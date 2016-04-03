package calculator

object Polynomial {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = Signal {
    val bVal = b()
    (bVal * bVal) - (4 * a() * c())
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = Signal{
    delta() match{
      case x if x < 0 => Set()
      case x if x == 0 => Set((-1 * b())/(2 * a()))
      case x =>
        val bVal = b()
        val aVal = a()
        val deltaSqrt = math.sqrt(x)
        Set((-bVal + deltaSqrt)/(2 * aVal), (-bVal - deltaSqrt)/(2 * aVal))
    }
  }
}

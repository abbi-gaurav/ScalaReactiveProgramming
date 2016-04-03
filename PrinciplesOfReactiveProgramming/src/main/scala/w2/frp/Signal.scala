package w2.frp

import scala.util.DynamicVariable

/**
 * Created by gabbi on 10/05/15.
 */
class Signal[T](expr: =>T) {
  import Signal._
  private var myExpr: () => T = _
  private var myValue:T = _
  private var observers:Set[Signal[_]] = Set()
  update(expr)

  def computeValue(): Unit = {
    val newValue = caller.withValue(this)(myExpr())
    if(newValue != myValue){
      myValue = newValue
      val obs = observers
      observers = Set()
      obs.foreach(_.computeValue())
    }
  }

  protected def update(expr: =>T):Unit = {
    myExpr = () => expr
    computeValue()
  }

  def apply():T = {
    observers += caller.value
    assert(!caller.value.observers.contains(this), "cyclic signal definition")
    myValue
  }
}

object NoSignal extends Signal[Nothing](???){
  override def computeValue(): Unit = ()
}

object Signal{
//  private var caller = new StackableVariable[Signal[_]](NoSignal)
  private val caller = new DynamicVariable[Signal[_]](NoSignal)
  def apply[T](expr: =>T) = new Signal[T](expr)
}

class Var[T](expr: =>T) extends Signal(expr){
  override def update(expr: =>T) = super.update(expr)
}

object Var {
  def apply[T](expr: =>T) = new Var(expr)
}

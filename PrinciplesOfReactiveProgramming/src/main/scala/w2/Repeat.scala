package w2

/**
 * Created by gabbi on 02/05/15.
 */
class Repeat(command: => Unit) {
  def until(condition: => Boolean):Unit = {
    command
    if(condition) () else until(condition)
  }
}

object Repeat{
  def apply(command: => Unit) = new Repeat(command)
}

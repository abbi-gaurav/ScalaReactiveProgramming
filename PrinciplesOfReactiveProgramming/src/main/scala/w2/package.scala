import scala.annotation.tailrec

/**
 * Created by gabbi on 02/05/15.
 */
package object w2 {
  def cons[T](hd:T, tl: => Stream[T]):Stream[T] = new Stream[T]{
    override def head = hd

    private var tailOpt:Option[Stream[T]] = None

    override def tail:Stream[T] = tailOpt match {
      case Some(x) => x
      case None => tailOpt = Some(tl); tail
    }

    override protected def tailDefined: Boolean = true

    override def isEmpty = false
  }

  @tailrec
  def WHILE(condition: => Boolean) (command: => Unit):Unit = {
    if(condition){
      command
      WHILE(condition)(command)
    }else ()
  }

  @tailrec
  def REPEAT(condition: => Boolean) (command: => Unit):Unit = {
    command
    if(condition) () else REPEAT(condition) (command)
  }
}

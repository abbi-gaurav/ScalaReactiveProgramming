package w1

/**
 * Created by gabbi on 22/04/15.
 */
package object random {
  val integers = new Generator[Int]{
    private val random = new java.util.Random
    override def generate = random.nextInt
  }

  val booleans:Generator[Boolean] = for{x <- integers}yield x > 0

  def pairs[T,U](t:Generator[T],u:Generator[U]):Generator[(T,U)] = for {
    x <- t
    y <- u
  }yield(x,y)

  val pairsInt = pairs(integers, integers)

  def single[T](x:T):Generator[T] = new Generator[T]{
    def generate:T = x
  }

  def choose(lo:Int, hi:Int):Generator[Int] = for{
    x <- integers
  }yield(lo + x % (hi-lo))

  def oneOf[T](xs:T*):Generator[T] = for{
    idx <- choose(0, xs.length)
  }yield xs(idx)

  def lists:Generator[List[Int]] = for{
    isEmpty <- booleans
    list <- if(isEmpty) emptyLists else nonEmptyLists
  }yield list

  val emptyLists:Generator[List[Int]] = single(Nil)

  def nonEmptyLists:Generator[List[Int]] = for{
    head <- integers
    tail <- lists
  }yield head::tail
}

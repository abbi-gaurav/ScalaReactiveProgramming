package w1.random

/**
 * Created by gabbi on 22/04/15.
 */
trait Tree
case class Inner(left:Tree, right:Tree) extends Tree
case class Leaf(x:Int) extends Tree

object RandomTree {
  def leafs:Generator[Leaf] = for{
    x <- integers
  }yield Leaf(x)

  def inners:Generator[Inner] = for{
    l <- trees
    r <- trees
  }yield Inner(l,r)

  def trees:Generator[Tree] = for{
    isLeaf <- booleans
    tree <- if(isLeaf) leafs else inners
  }yield tree
}

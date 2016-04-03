package quickcheck

import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.Prop._
import org.scalacheck._

import scala.annotation.tailrec

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  property("min1") = forAll { a: Int =>
    val h = insert(a, empty)
    findMin(h) == a
  }
  property("gen1") = forAll { h: H =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  property("insert2") = forAll { (x: Int, y: Int) =>
    val h = insert(x, insert(y, empty))
    findMin(h) == Math.min(x, y)
  }

  property("deleteSingle") = forAll { x: Int =>
    val h: H = insert(x, empty)

    !isEmpty(h) && isEmpty(deleteMin(h))
  }
  property("ordering") = forAll { h: H =>
    @tailrec
    def toList(heap: H, list: List[Int]): List[Int] = {
      if (isEmpty(heap)) list
      else toList(deleteMin(heap), findMin(heap) :: list)

    }

    @tailrec
    def isSorted(heap: H): Boolean = {
      if (isEmpty(heap)) true
      else {
        val min = findMin(heap)
        val newHeap = deleteMin(heap)
        isEmpty(newHeap) || min <= findMin(newHeap) && isSorted(newHeap)
      }
    }
    //    val list = toList(h, Nil)
    //    list.sortWith{case (x,y) => x > y} == list
    isSorted(h)
  }
  property("melding") = forAll { (h1: H, h2: H) =>
    findMin(meld(h1, h2)) == Math.min(findMin(h1), findMin(h2))
  }

  property("meld two empty heaps") = forAll { x: Int =>
    isEmpty(meld(empty, empty))
  }

  property("meld 2 ways same heaps") = forAll { (h1: H, h2: H) =>
    heapEquals(meld(h1, h2), meld(deleteMin(h1), insert(findMin(h1), h2)))
  }

  property("meld and delete min should be same as not having min") = forAll{(h1:H, h2:H) =>
    if (findMin(h1) < findMin(h2)){
     heapEquals(deleteMin(meld(h1,h2)), meld(deleteMin(h1), h2))
    }else {
      heapEquals(deleteMin(meld(h1, h2)), meld(h1, deleteMin(h2)))
    }
  }
  lazy val genHeap: Gen[H] = for {
    k <- arbitrary[Int]
    h <- frequency((1, const(empty)), (9, genHeap))
  } yield insert(k, h)

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  @tailrec
  private def heapEquals(heap1: H, heap2: H): Boolean = {
    if (isEmpty(heap1) && isEmpty(heap2)) true
    else {
      val h1Min = findMin(heap1)
      val h2Min = findMin(heap2)
      h1Min == h2Min && heapEquals(deleteMin(heap1), deleteMin(heap2))
    }
  }
}

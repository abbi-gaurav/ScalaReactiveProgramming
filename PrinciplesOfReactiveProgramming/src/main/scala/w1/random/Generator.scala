package w1.random

/**
 * Created by gabbi on 22/04/15.
 */
trait Generator[+T] {
  self => //an alias for this

  def generate:T

  def map[S](f: T=>S):Generator[S] = new Generator[S]{
    def generate:S = f(self.generate)  //this.generate will be a infinite recursive call
  }

  def flatMap[S](f: T=>Generator[S]):Generator[S] = new Generator[S]{
    def generate:S = f(self.generate).generate
  }
}

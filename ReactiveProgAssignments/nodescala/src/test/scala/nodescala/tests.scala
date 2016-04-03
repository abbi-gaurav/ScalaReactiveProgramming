package nodescala

import scala.language.postfixOps
import scala.util.{Try, Success, Failure}
import scala.collection._
import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.async.Async.{async, await}
import org.scalatest._
import NodeScala._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NodeScalaSuite extends FunSuite {

  test("A Future should always be completed") {
    val always = Future.always(517)

    assert(Await.result(always, 0 nanos) == 517)
  }
  test("A Future should never be completed") {
    val never = Future.never[Int]
    try {
      Await.result(never, 1 second)
      assert(false)
    } catch {
      case t: TimeoutException => // ok!
    }
  }

  test("all successfull futures should return with correct values"){
    val alls: Future[List[Int]] = Future.all(List(Future(1), Future(2), Future(3)))
    assert(Await.result(alls, Duration.Inf) == List(1,2,3))
  }

  test("one failure with many other successfull futures should return with failure value"){
    val alls: Future[List[Int]] = Future.all(List(Future(1), Future.failed(new Exception("dummy")), Future(2), Future(3)))
    try{
      Await.result(alls, 1 second)
      assert(false)
    }catch {
      case t:Exception => assert(t.getLocalizedMessage == "dummy")
    }
  }

  test("any with all never except one with always complete with that one"){
    val any = Future.any(List(Future.never, Future(1), Future.never))
    assert(Await.result(any, 1 second) == 1)
  }

  test("any with all never except one failure with always complete with that failure"){
    val any = Future.any(List(Future.never, Future.failed(new Exception("dummy")), Future.never))
    try{
      Await.result(any, 1 second)
      assert(false)
    }catch {
      case t:Exception => assert(t.getLocalizedMessage == "dummy")
    }
  }

  test("wait longer than delay should succees"){
    val f = Future.delay(1 second)
    try{
      Await.result(f, 3 seconds)
    }catch{
      case ex:Exception => assert(false)
    }
  }

  test("a wait shorter than delay should fail"){
    val f = Future.delay(3 second)
    try{
      Await.result(f, 1 seconds)
      assert(false)
    }catch{
      case ex:Exception => //ok
    }
  }

  test("A Future should not complete after 1s when using a delay of 3s") {
    try {
      val p = Future.delay(3 second)
      val z = Await.result(p, 1 second) // block for future to complete
      assert(false)
    } catch {
      case _: TimeoutException => // Ok!
    }
  }

  private def getPromise(duration: FiniteDuration): Promise[Int] = {
    val p = Promise[Int]()
    Future {
      blocking {
        Future.delay(duration) onSuccess {
          case _ => p.complete(Try(1))
        }
      }
    }
    p
  }

  class DummyExchange(val request: Request) extends Exchange {
    @volatile var response = ""
    val loaded = Promise[String]()
    def write(s: String) {
      response += s
    }
    def close() {
      loaded.success(response)
    }
  }

  class DummyListener(val port: Int, val relativePath: String) extends NodeScala.Listener {
    self =>

    @volatile private var started = false
    var handler: Exchange => Unit = null

    def createContext(h: Exchange => Unit) = this.synchronized {
      assert(started, "is server started?")
      handler = h
    }

    def removeContext() = this.synchronized {
      assert(started, "is server started?")
      handler = null
    }

    def start() = self.synchronized {
      started = true
      new Subscription {
        def unsubscribe() = self.synchronized {
          started = false
        }
      }
    }

    def emit(req: Request) = {
      val exchange = new DummyExchange(req)
      if (handler != null) handler(exchange)
      exchange
    }
  }

  class DummyServer(val port: Int) extends NodeScala {
    self =>
    val listeners = mutable.Map[String, DummyListener]()

    def createListener(relativePath: String) = {
      val l = new DummyListener(port, relativePath)
      listeners(relativePath) = l
      l
    }

    def emit(relativePath: String, req: Request) = this.synchronized {
      val l = listeners(relativePath)
      l.emit(req)
    }
  }
  test("Server should serve requests") {
    val dummy = new DummyServer(8191)
    val dummySubscription = dummy.start("/testDir") {
      request => for (kv <- request.iterator) yield (kv + "\n").toString
    }

    // wait until server is really installed
    Thread.sleep(500)

    def test(req: Request) {
      val webpage = dummy.emit("/testDir", req)
      val content = Await.result(webpage.loaded.future, 1 second)
      val expected = (for (kv <- req.iterator) yield (kv + "\n").toString).mkString
      assert(content == expected, s"'$content' vs. '$expected'")
    }

    test(immutable.Map("StrangeRequest" -> List("Does it work?")))
    test(immutable.Map("StrangeRequest" -> List("It works!")))
    test(immutable.Map("WorksForThree" -> List("Always works. Trust me.")))

    dummySubscription.unsubscribe()
  }

}





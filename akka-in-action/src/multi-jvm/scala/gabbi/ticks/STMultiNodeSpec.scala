package gabbi.ticks

import akka.remote.testkit.MultiNodeSpecCallbacks
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpecLike}

trait STMultiNodeSpec extends MultiNodeSpecCallbacks
  with WordSpecLike with MustMatchers with BeforeAndAfterAll {

  override protected def beforeAll(): Unit = multiNodeSpecBeforeAll()

  override protected def afterAll(): Unit = multiNodeSpecAfterAll()
}

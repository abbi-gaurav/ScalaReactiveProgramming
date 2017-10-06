package api.common.actors

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import api.common.actors.TicketSeller._
import base.StopSystemAfterAll
import org.scalatest.{MustMatchers, WordSpecLike}

import scala.collection.immutable
import scala.language.postfixOps

class TicketSellerSpec extends TestKit(ActorSystem("ticket-seller-test"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {
  "The ticket seller" must {
    "sell tickets until they are sold out" in {
      def makeTickets: Vector[Ticket] = (1 to 10) map Ticket toVector

      val event: String = "RHCP"
      val ticketingActor: ActorRef = system.actorOf(TicketSeller.props(event))

      ticketingActor ! Add(makeTickets)

      ticketingActor ! Buy(1)
      expectMsg(Tickets(event, Vector(Ticket(1))))

      val nrs = 2 to 10
      nrs foreach (_ => ticketingActor ! Buy(1))
      val tickets: immutable.Seq[AnyRef] = receiveN(9)
      tickets.zip(nrs).foreach {
        case (Tickets(e, Vector(Ticket(id))), idx) =>
          e mustBe event
          id mustBe idx
      }

      ticketingActor ! Buy(1)
      expectMsg(Tickets(event))
    }
    "sell tickets in batches until they are sold out" in {
      val firstBatchSize = 10
      val subsequentBatchSize = 5
      val subsequentBatches = 18

      val event = "ScalaDays"
      val ticketingActor = system.actorOf(TicketSeller.props(event))

      def makeTickets: Vector[Ticket] = (1 to (10 * firstBatchSize)).map(Ticket).toVector

      ticketingActor ! Add(makeTickets)

      ticketingActor ! Buy(firstBatchSize)
      val firstPurchase: Vector[Ticket] = (1 to firstBatchSize).map(Ticket).toVector
      expectMsg(Tickets(event, firstPurchase))

      (1 to subsequentBatches).foreach(_ => ticketingActor ! Buy(subsequentBatchSize))
      val subsequentTicketsPurchased: immutable.Seq[AnyRef] = receiveN(subsequentBatches)
      subsequentTicketsPurchased mustBe a[Seq[Tickets]]
      subsequentTicketsPurchased.size mustBe 18
      val subsequentBatchTicketStart = (0 until subsequentBatches).map(x => (x * subsequentBatchSize) + 11)
      subsequentTicketsPurchased.asInstanceOf[Seq[Tickets]].zip(subsequentBatchTicketStart).foreach { case (subsequentPurchase: Tickets, idx) =>
        val expected = (idx until (idx + subsequentBatchSize)).map(Ticket).toVector
        subsequentPurchase.entries mustBe expected
      }

      ticketingActor ! Buy(1)
      expectMsg(Tickets(event))

      ticketingActor ! Buy(10)
      expectMsg(Tickets(event))
    }
  }
}

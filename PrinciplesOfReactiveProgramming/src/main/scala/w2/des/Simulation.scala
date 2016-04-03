package w2.des

/**
 * Created by gabbi on 03/05/15.
 */
trait Simulation {
  type Action = () => Unit

  case class Event(time: Int, action: Action)

  private type Agenda = List[Event]

  private var agenda: Agenda = List()

  private var curTime = 0

  def currentTime: Int = curTime

  private def insert(list: List[Event], event: Event): List[Event] = list match {
    case hd :: rest if (hd.time <= event.time) => hd :: insert(rest, event)
    case _ => event :: list
  }

  def afterDelay(delay: Int)(block: => Unit): Unit = {
    val item = Event(delay + currentTime, () => block)
    agenda = insert(agenda, item)
  }

  private def loop(): Unit = agenda match {
    case first :: rest =>
      agenda = rest
      curTime = first.time
      first.action()
      loop()
    case Nil =>
  }

  def run(): Unit = {
    afterDelay(0) {
      println(s"Simulation started at ${currentTime}")
    }
    loop()
  }
}

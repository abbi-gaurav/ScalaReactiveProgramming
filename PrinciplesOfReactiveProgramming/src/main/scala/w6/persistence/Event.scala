package w6.persistence

/**
 * Created by gabbi on 27/09/15.
 */
sealed trait Event

case class PostCreated(text: String) extends Event

case object QuotaReached extends Event

//aggregate
case class State(posts: Vector[String], disabled: Boolean) {
  def updated(e: Event): State = e match {
    case PostCreated(text: String) => copy(posts = posts :+ text)
    case QuotaReached => copy(disabled = true)
  }
}

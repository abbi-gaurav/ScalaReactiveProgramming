package learn.akka.cluster.sample

sealed trait Role{
  def getDomainString: String = toString.toLowerCase
}

case object Seed extends Role

case object Consumer extends Role

case object Producer extends Role

object Role {
  def apply(string: String): Role = string.toLowerCase match {
    case "producer" => Producer
    case "consumer" => Consumer
    case "seed" => Seed
    case _ => Seed
  }
}
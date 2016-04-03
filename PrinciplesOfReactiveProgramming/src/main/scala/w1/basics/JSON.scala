package w1.basics

/**
 * Created by gabbi on 16/04/15.
 */
abstract class JSON

case class JString(str: String) extends JSON

case class JNumber(num: Number) extends JSON

case class JSeq(elems: Seq[JSON]) extends JSON

case object JNull extends JSON

case class JObj(bindings: Map[String, JSON]) extends JSON

case class JBool(boolean: Boolean) extends JSON

object JSON {
  def show(json: JSON): String = json match {
    case JString(str) => "\"" + str + "\""
    case JNumber(number) => number.toString
    case JSeq(elems) => "[" + (elems map show mkString ",") + "]"
    case JObj(bindings) =>
      val assocs = bindings map {
        case (key, value) => "\"" + key + "\":" + show(value)
      }
      "{" + assocs mkString "," + "}"
    case JBool(b) => b.toString
    case JNull => "null"
  }
}

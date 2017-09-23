package learn.akka.cluster.sample

sealed trait Message

case object SimpleMessage extends Message

package com.learn.akka.http.withModel

import akka.Done
import akka.actor.Actor

/**
  * Created by gabbi on 19/02/2017.
  */
class DB extends Actor {
  import DB._
  private var map: Map[Long, Item] = Map()

  override def receive: Receive = {
    case Add(items) =>
      map = items.foldRight(map) {
        case (item, acc) => acc.updated(item.id, item)
      }
      sender() ! Done

    case Fetch(id) => sender() ! map.get(id)
  }
}

object DB{
  case class Add(items: List[Item])

  case class Fetch(id: Long)
}

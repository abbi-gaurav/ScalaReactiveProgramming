package com.learn.akka.http.withModel

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, JsonFormat}

/**
  * Created by gabbi on 19/02/2017.
  */
case class Item(name: String, id: Long)

case class Order(items: List[Item])

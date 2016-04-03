package w5.urlFetcher

import akka.actor.{ActorRef, Actor}
import akka.actor.Actor.Receive
import w5.urlFetcher.Cache.{Result, Get}
import akka.pattern.pipe
/**
 * Created by gabbi on 07/06/15.
 */
class Cache extends Actor{
  var cache = Map.empty[String,String]
  private implicit val exe = context.dispatcher
  override def receive: Receive = {
    case Get(url) =>
      if(cache contains url) sender() ! cache(url)
      else{
        val client = sender()
        AsyncWebClient get url map (Result(client, url, _)) pipeTo self
      }
    case Result(client, url, body) =>
      cache += url -> body
      client ! body
  }
}

object Cache{
  case class Get(url:String)
  case class Result(client:ActorRef, url:String, body:String)
}
package learn.akka.stream.largeDataSets

import akka.http.scaladsl.server.{Directive1, Directives}

/**
  * Created by gabbi on 25.06.17.
  */
trait ServiceDirectives extends Directives{
  val pageParams:Directive1[PageParams] = parameters(('pageNumber.as[Int].?,'pageSize.as[Int].?)).as(PageParams)
}

package learn.akka.stream.largeDataSets

/**
  * Created by gabbi on 25.06.17.
  */
case class PageParams(pageSize: Option[Int], pageNumber: Option[Int]) {
  private val defaultPageNumber = 1
  private val defaultPageSize = 16

  val limit = pageSize.getOrElse(defaultPageSize)
  val skip = pageNumber.getOrElse(defaultPageNumber - 1) * limit
}

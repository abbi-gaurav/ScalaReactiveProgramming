package w5.urlFetcher

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

/**
 * Created by gabbi on 07/06/15.
 */
object Utils {
  def findLinks(body: String, baseUri: String): Iterator[String] = {
    val document: Document = Jsoup.parse(body, baseUri)
    val links: Elements = document.select("a[href]")
    for {
      link <- links.iterator().asScala
    } yield link.absUrl("href")

  }
}

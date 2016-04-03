package w5.urlFetcher

import java.util.concurrent.Executor

import com.ning.http.client.AsyncHttpClient

import scala.concurrent.{Future, Promise}

/**
 * Created by gabbi on 07/06/15.
 */

trait WebClient {
  def get(url: String)(implicit executor: Executor): Future[String]

  def shutdown(): Unit
}

case class BadStatus(statusCode: Int) extends RuntimeException

object AsyncWebClient extends WebClient {

  private val asyncClient = new AsyncHttpClient()

  def get(url: String)(implicit executor: Executor): Future[String] = {
    val future = asyncClient.prepareGet(url).execute()
    val promise = Promise[String]()
    future.addListener(new Runnable {
      override def run(): Unit = {
        val response = future.get()
        if (response.getStatusCode < 400) {
          promise.success(response.getResponseBodyExcerpt(131072))
        } else {
          promise.failure(BadStatus(response.getStatusCode))
        }
      }
    }, executor)
    promise.future
  }

  def shutdown(): Unit = asyncClient.close()
}

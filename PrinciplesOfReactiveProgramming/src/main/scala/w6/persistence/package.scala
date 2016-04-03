package w6

/**
 * Created by gabbi on 27/09/15.
 */
package persistence {

//command
case class NewPost(text: String, id: Long)

//replies
case class BlogPosted(id: Long)

case class BlogNotPosted(id: Long, reason: String)

case class PublishPost(text: String, deliveryId: Long)

case class PostPublished(id: Long)

}

package common.helper

import common.exceptions._
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results._


import scala.concurrent.{Promise, Future}

object ImplicitConversions {
  implicit class OptionConverter[T](x: java.util.Optional[T]) {
    def asScala: Option[T] = {
      if (x.isPresent) Some[T](x.get())
      else Option.empty[T]
    }
  }

  implicit class ExceptionToResultConverter(x: Throwable) {
    def asResult: Result = {
      x match {
        case e: SphereIndexFailed => InternalServerError(errorJson(x.getMessage))
        case e: ProductNotFound => NotFound(errorJson(x.getMessage))
        case e: ElasticSearchIndexFailed => InternalServerError(errorJson(x.getMessage))
        case e: ElasticSearchQueryFailed => InternalServerError(errorJson(x.getMessage))
        case e: CustomerAlreadyExists => Conflict(x.getMessage)
        case e: InvalidJson => BadRequest(errorJson(x.getMessage))
        case e: Unauthorized => Unauthorized(errorJson(x.getMessage))
        case _ => InternalServerError(errorJson(x.getMessage))
      }
    }

    def errorJson(message: String) = Json.obj("error" -> message)
  }

  implicit class ActionListenableFutureConverter[T](x: ListenableActionFuture[T]) {
    def asScala: Future[T] = {
      val p = Promise[T]()
      x.addListener(new ActionListener[T] {
        def onFailure(e: Throwable) = p.failure(e)
        def onResponse(response: T) = p.success(response)
      })
      p.future
    }
  }
}

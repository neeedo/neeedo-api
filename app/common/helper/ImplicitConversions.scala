package common.helper

import common.exceptions.{ElasticSearchIndexFailed, ProductNotFound}
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import play.api.libs.json.{JsObject, Json}
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
        case e: ProductNotFound => NotFound(errorJson(x.getMessage))
        case e: ElasticSearchIndexFailed => InternalServerError(errorJson(x.getMessage))
        case _ => BadRequest(errorJson(x.getMessage))
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

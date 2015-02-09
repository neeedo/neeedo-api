package common.helper

import org.elasticsearch.action.{ActionListener, ListenableActionFuture}

import scala.concurrent.{Promise, Future}

object ImplicitConversions {
  implicit class OptionConverter[T](x: java.util.Optional[T]) {
    def asScala: Option[T] = {
      if (x.isPresent) Some[T](x.get())
      else Option.empty[T]
    }
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

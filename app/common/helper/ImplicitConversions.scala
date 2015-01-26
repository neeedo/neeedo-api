package common.helper

import org.elasticsearch.action.{ActionListener, ListenableActionFuture}

import scala.concurrent.{Promise, Future}

object ImplicitConversions {
  implicit def optionalToOption[T](opt: java.util.Optional[T]): Option[T] = {
    if (opt.isPresent) Some[T](opt.get())
    else Option.empty[T]
  }

  implicit def convertListenableActionFutureToScalaFuture[T](x: ListenableActionFuture[T]): Future[T] = {
    val p = Promise[T]()
    x.addListener(new ActionListener[T] {
      def onFailure(e: Throwable) = p.failure(e)
      def onResponse(response: T) = p.success(response)
    })
    p.future
  }
}

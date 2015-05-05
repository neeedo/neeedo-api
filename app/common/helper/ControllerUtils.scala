package common.helper

import play.api.libs.json.Reads
import play.api.mvc.AnyContent

import scala.util.Try

object ControllerUtils {
  def bindRequestJsonBody[T](body: AnyContent)(implicit tjs: Reads[T]): Try[T] = {
    Try {
      body.asJson match {
        case Some(json) =>
          json.asOpt[T] match {
            case Some(draft) => draft
            case None => throw new IllegalArgumentException("Cannot parse json")
          }
        case None => throw new IllegalArgumentException("Missing body")
      }
    }
  }
}

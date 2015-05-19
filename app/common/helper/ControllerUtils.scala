package common.helper

import common.exceptions.InvalidJson
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
            case None => throw new InvalidJson(s"Invalid json body")
          }
        case None => throw new InvalidJson("Missing body json object")
      }
    }
  }
}

package common.helper

import common.exceptions.{InvalidJson, Unauthorized}
import play.api.libs.json.{JsObject, Reads}
import play.api.mvc.{AnyContent, Request}

import scala.util.Try

trait ControllerUtils {
  def bindRequestJsonBody[T](request: Request[AnyContent])(implicit tjs: Reads[T]): Try[T] = {
    Try {
      request.body.asJson match {
        case Some(json) =>
          json.asOpt[T] match {
            case Some(draft) => handleSecuredRequest(request, draft)
            case None => throw new InvalidJson("Invalid json body")
          }
        case None => throw new InvalidJson("Missing body json object")
      }
    }
  }

  private[helper] def handleSecuredRequest[T](request: Request[AnyContent], draft: T) = {
    request match {
      case req: SecuredRequest[AnyContent] =>
        if (isAuthorized(req)) draft
        else throw new Unauthorized("You have not sufficient rights to this")
      case _ => draft
    }
  }

  private[helper] def isAuthorized(request: SecuredRequest[AnyContent]) = {
    val json = request.body.asJson.get.as[JsObject]
    if (json.keys.contains("userId"))
      (json \\ "userId").head.as[String] == request.userId.value
    else true
  }
}

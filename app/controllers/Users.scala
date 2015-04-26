package controllers

import common.domain.{UserDraft, UserId}
import common.helper.SecuredAction
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.UserService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Users(userService: UserService) extends Controller {
  def getUserByMail(mail: String) = SecuredAction.async {
//    userService.getUserByMail(mail).map {
//      case Some(user) => Ok(Json.toJson(user))
//      case None => NotFound
//    }
    Future.successful(Ok)
  }

  def createUser = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[UserDraft] match {
          case Some(draft) => userService.createUser(draft).map {
            case Some(user) => Created(Json.obj("user" -> Json.toJson(user)))
            case _ => BadRequest(Json.obj("error" -> "Unknown error"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def updateUser(id: UserId, version: common.domain.Version) = Action.async {
    Future.successful(Ok)
  }

  def deleteUser(id: UserId, version: common.domain.Version) = Action.async {
    Future.successful(Ok)
  }

  //
  //  def decodeBase64: String = {
  //    new String(new sun.misc.BASE64Decoder().decodeBuffer(s), Charset.forName("UTF-8"))
  //  }
}

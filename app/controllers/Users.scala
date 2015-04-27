package controllers

import common.domain.{Version, UserDraft, UserId}
import common.helper.SecuredAction
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.UserService
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class Users(userService: UserService) extends Controller {

  // Todo enable secured actions, currently gives 301
  def getUserByMail(mail: String) = SecuredAction.async {
    userService.getUserByEmail(mail).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
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

  def updateUser(id: UserId, version: Version) = SecuredAction.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.asOpt[UserDraft] match {
        case Some(draft) => userService.updateUser(id, version, draft).map {
          case Some(user) => Ok(Json.obj("user" -> Json.toJson(user)))
          case _ => BadRequest(Json.obj("error" -> "Unknown error"))
        }
        case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def deleteUser(id: UserId, version: Version) = SecuredAction.async {
    userService.deleteUser(id, version).map {
      case Some(_) => Ok
      case None => NotFound
    }
  }
}

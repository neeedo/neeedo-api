package controllers

import common.domain.{Email, Version, UserDraft, UserId}
import common.helper.{ControllerUtils, SecuredAction}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.UserService
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.ImplicitConversions.ExceptionToResultConverter
import scala.concurrent.Future
import scala.util.{Failure, Success}
import ControllerUtils.bindRequestJsonBody

class Users(userService: UserService) extends Controller {

  // Todo enable secured actions, currently gives 301
  def getUserByMail(mail: Email) = SecuredAction.async {
    userService.getUserByEmail(mail).map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
  }

  def createUser = Action.async { implicit request =>
    val userDraft = bindRequestJsonBody(request.body)(UserDraft.userDraftReads)

    userDraft match {
      case Failure(e) => Future(e.asResult)
      case Success(u) =>
        userService.createUser(u).map {
          user => Created(Json.obj("user" -> Json.toJson(user)))
        } recover {
          case e: Exception => e.asResult
        }
    }
  }

  def updateUser(id: UserId, version: Version) = SecuredAction.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.asOpt[UserDraft] match {
        case Some(draft) =>
          userService.updateUser(id, version, draft).map {
            user => Ok(Json.obj("user" -> Json.toJson(user)))
          } recover {
            case e: Exception => e.asResult
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

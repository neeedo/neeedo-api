package controllers

import common.domain.{Email, Version, UserDraft, UserId}
import common.helper.{ControllerUtils, SecuredAction}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.sphere.SphereUserService
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.ImplicitConversions.ExceptionToResultConverter
import scala.concurrent.Future
import scala.util.{Failure, Success}

class UsersController(userService: SphereUserService, securedAction: SecuredAction)
  extends Controller with ControllerUtils {

  def getUserByMail(mail: Email) = securedAction.async {
    userService.getUserByEmail(mail) map {
      case Some(user) => Ok(Json.toJson(user))
      case None => NotFound
    }
  }

  def getUserById(id: UserId) = securedAction.async {
    userService.getUserById(id) map {
      userIdAndName => Ok(Json.toJson(userIdAndName))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def createUser = Action.async { implicit request =>
    val userDraft = bindRequestJsonBody(request)(UserDraft.userDraftReads)

    userDraft match {
      case Success(u) =>
        userService.createUser(u) map {
          user => Created(Json.toJson(user))
        } recover {
          case e: Exception => e.asResult
        }
      case Failure(e) => Future(e.asResult)
    }
  }

  def deleteUser(id: UserId, version: Version) = securedAction.async {
    userService.deleteUser(id, version) map {
      case _ => Ok
    } recover {
      case e: Exception => e.asResult
    }
  }
}

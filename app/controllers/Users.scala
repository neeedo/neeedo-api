package controllers

import common.domain.{UserId, Username}
import common.helper.SecuredAction
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.UserService

import scala.concurrent.Future

class Users(userService: UserService) extends Controller {
  def getUserByMail(mail: String) = SecuredAction.async {
//    userService.getUserByMail(mail).map {
//      case Some(user) => Ok(Json.toJson(user))
//      case None => NotFound
//    }
    Future.successful(Ok)
  }

  def createUser() = SecuredAction.async {
    Future.successful(Ok)
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

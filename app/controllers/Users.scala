package controllers

import common.domain.{UserId, Username}
import common.helper.SecuredAction
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

class Users extends Controller {
  def getUserByName(id: Username) = SecuredAction.async {
    Future.successful(Ok)
  }

  def createUser() = Action.async {
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

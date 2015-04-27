package common.helper

import common.domain.{Email, UserCredentials}
import org.apache.commons.codec.binary.Base64
import play.api.{Mode, Play}
import play.api.http.HeaderNames._
import play.api.mvc.{Action, Result, Request, ActionBuilder}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Results._
import services.UserService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SecuredAction extends ActionBuilder[Request] {

  def invokeBlock[A](request: Request[A], block: (Request[A]) => Future[Result]): Future[Result] = {
    block(request)
  }

  override def composeAction[A](action: Action[A]) = new SecuredAction(action)
}

case class SecuredAction[A](action: Action[A]) extends Action[A] {

  lazy val parser = action.parser

  def apply(request: Request[A]): Future[Result] = {
    if (isUnsecure(request)) redirectHttps(request)
    else authorize(request)
  }

  def isAuthorized(userCredentials: UserCredentials): Future[Boolean] = {
    if (Play.current.mode == Mode.Test)
      Future { userCredentials.email == Email("test") && userCredentials.password == "test" }
    else
      UserService.authorizeUser(userCredentials)
  }

  def authorize(request: Request[A]): Future[Result] = {
    val authHeader: String = request.headers.get(AUTHORIZATION).getOrElse("")
    val userCredentialsOption = getCredentialsFromAuthHeader(authHeader)
    userCredentialsOption match {
      case Some(userCredentials) =>
        isAuthorized(userCredentials).flatMap {
          result =>
            if (result) action(request)
            else Future.successful(Unauthorized)
        }
      case None => requestAuthorization
    }
  }

  def redirectHttps(request: Request[A]): Future[Result] = {
    Future.successful(MovedPermanently(s"https://${request.domain}${request.uri}"))
  }

  def requestAuthorization: Future[Result] = {
    Future.successful(Unauthorized.withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured""""))
  }

  def isUnsecure(request: Request[A]) = !request.secure

  def getCredentialsFromAuthHeader(authHeader: String): Option[UserCredentials] = {
    def getToken(authHeader: String): Option[String] = authHeader.split(" ").drop(1).headOption

    getToken(authHeader).flatMap { encodedToken =>
      new String(Base64.decodeBase64(encodedToken.getBytes)).split(":").toList match {
        case List(email, password) => Some(UserCredentials(Email(email), password))
        case _ => None
      }
    }
  }
}


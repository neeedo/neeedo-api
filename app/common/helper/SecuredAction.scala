package common.helper

import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames._
import play.api.mvc.{Action, Result, Request, ActionBuilder}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Results._

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
    else authorize(request) getOrElse requestAuthorization
  }

  def isAuthorized(userCredentials: UserCredentials): Future[Boolean] = Future.successful(true)

  def authorize(request: Request[A]): Option[Future[Result]] = {
    request.headers.get(AUTHORIZATION).flatMap {
      authHeader: String => {
        val userCredentialsOption = getCredentialsFromAuthHeader(authHeader)
        userCredentialsOption.collect {
          case credentials => isAuthorized(credentials)
        }.map(_ => action(request))
      }
    }
  }

  def redirectHttps(request: Request[A]): Future[Result] = {
    Future.successful(MovedPermanently(s"https://${request.domain}${request.uri}"))
  }

  def requestAuthorization: Future[Result] = {
    Future.successful(Unauthorized.withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured""""))
  }


  def isValid(user: String, password: String): Boolean = true

  def isUnsecure(request: Request[A]) = !request.secure

  def getCredentialsFromAuthHeader(authHeader: String): Option[UserCredentials] = {
    def getToken(authHeader: String): Option[String] = authHeader.split(" ").drop(1).headOption

    getToken(authHeader).flatMap { encodedToken =>
      new String(Base64.decodeBase64(encodedToken.getBytes)).split(":").toList match {
        case List(username, password) => Some(UserCredentials(username, password))
        case _ => None
      }
    }
  }

  case class UserCredentials(user: String, pw: String)
}


package common.helper

import common.domain._
import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames._
import play.api.mvc._
import play.api.mvc.Results.Unauthorized
import play.api.mvc.Results._
import services.UserService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SecuredAction(userService: UserService) extends ActionBuilder[SecuredRequest] {

  def invokeBlock[A](request: Request[A], block: (SecuredRequest[A]) => Future[Result]): Future[Result] = {
    if (request.secure) authorize(request, block)
    else redirectHttps(request)
  }

  def redirectHttps[A](request: Request[A]): Future[Result] = {
    Future.successful(MovedPermanently(s"https://${request.domain}${request.uri}"))
  }

  def authorize[A](request: Request[A], block: (SecuredRequest[A]) => Future[Result]): Future[Result] = {
    val authHeader: String = request.headers.get(AUTHORIZATION).getOrElse("")
    val userCredentialsOption = getCredentialsFromAuthHeader(authHeader)
    userCredentialsOption match {
      case Some(userCredentials) =>
        isAuthorized(userCredentials).flatMap {
          case Some(userId) => block(new SecuredRequest[A](userId, request))
          case None => Future.successful(Forbidden)
        }
      case None => requestAuthorization
    }
  }

  def getCredentialsFromAuthHeader(authHeader: String): Option[UserCredentials] = {
    def getToken(authHeader: String): Option[String] = authHeader.split(" ").drop(1).headOption

    getToken(authHeader).flatMap { encodedToken =>
      new String(Base64.decodeBase64(encodedToken.getBytes)).split(":").toList match {
        case List(email, password) => Some(UserCredentials(Email(email), Password(password)))
        case _ => None
      }
    }
  }

  def isAuthorized(userCredentials: UserCredentials): Future[Option[UserId]] = {
    userService.authorizeUser(userCredentials)
  }

  def requestAuthorization: Future[Result] = {
    Future.successful(Unauthorized.withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured""""))
  }
}

class SecuredRequest[A](val userId: UserId, request: Request[A]) extends WrappedRequest[A](request)
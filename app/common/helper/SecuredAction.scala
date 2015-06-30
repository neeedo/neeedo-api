package common.helper

import common.domain._
import common.helper.ImplicitConversions.ExceptionToResultConverter
import org.apache.commons.codec.binary.Base64
import play.api.http.HeaderNames._
import play.api.mvc.Results.{Unauthorized, _}
import play.api.mvc._
import services.sphere.SphereUserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SecuredAction(userService: SphereUserService) extends ActionBuilder[SecuredRequest] {

  def invokeBlock[A](request: Request[A], block: (SecuredRequest[A]) => Future[Result]): Future[Result] = {
    if (request.secure) authorize(request, block)
    else redirectHttps(request)
  }

  def redirectHttps[A](request: Request[A]): Future[Result] = {
    Future.successful(MovedPermanently(s"https://${request.domain}${request.uri}"))
  }

  def authorize[A](request: Request[A], block: (SecuredRequest[A]) => Future[Result]): Future[Result] = {
    val authHeader = request.headers.get(AUTHORIZATION).getOrElse("")

    getCredentialsFromAuthHeader(authHeader) match {
      case Some(credentials) =>
        userService.authorizeUser(credentials) flatMap {
          case Some(userId) => block(new SecuredRequest[A](userId, request))
          case None => Future.successful(Forbidden)
        } recover {
          case e: Exception => e.asResult
        }
      case _ => requestAuthorization
    }
  }

  def getCredentialsFromAuthHeader(authHeader: String): Option[UserCredentials] = {
    def getToken(authHeader: String): Option[String] = authHeader.split(" ").drop(1).headOption
    def decodeToken(token: String) = new String(Base64.decodeBase64(token.getBytes)).split(":").toList

    getToken(authHeader) flatMap {
      encodedToken =>
        decodeToken(encodedToken) match {
          case List(email, password) => Option(UserCredentials(Email(email), Password(password)))
          case _ => None
        }
    }
  }

  val requestAuthorization = Future(Unauthorized
    .withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured""""))
}

class SecuredRequest[A](val userId: UserId, request: Request[A]) extends WrappedRequest[A](request)
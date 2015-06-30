package common.helper

import common.domain.{Email, Password, UserCredentials}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.http.HeaderNames._
import play.api.mvc.Results._
import play.api.mvc.{AnyContentAsEmpty, AnyContent}
import play.api.test.{FakeHeaders, Helpers, FakeRequest}
import services.sphere.SphereUserService

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SecuredActionSpec extends Specification with Mockito {

  trait SecuredActionContext extends Scope {
    val httpRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(),
      AnyContentAsEmpty)

    val userService = mock[SphereUserService]
    val securedAction = new SecuredAction(userService)

    val validHeader = "Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="
    val invalidHeader = "QWxhZGRpbjpvcGVuIHNlc2FtZQ=="

    val credentials = UserCredentials(Email("Aladdin"), Password("open sesame"))
  }

  "SecuredAction" should {
    "redirectHttps must return moved directly request with https domain" in new SecuredActionContext {
      Await.result(securedAction.redirectHttps(httpRequest), Duration.Inf).toString() must
        beEqualTo(MovedPermanently("https:///users").toString())
    }

    "getCredentialsFromAuthHeader must return correct UserCredentials" in new SecuredActionContext {
      securedAction.getCredentialsFromAuthHeader(validHeader) mustEqual Option(credentials)
    }

    "getCredentialsFromAuthHeader must return none for invalid header" in new SecuredActionContext {
      securedAction.getCredentialsFromAuthHeader(invalidHeader) mustEqual Option.empty[UserCredentials]
    }

    "requestAuthorization must return result with WWW_Authenticat header" in new SecuredActionContext {
      Await.result(securedAction.requestAuthorization, Duration.Inf).toString() must
        beEqualTo(Unauthorized.withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured"""").toString())
    }
  }
}

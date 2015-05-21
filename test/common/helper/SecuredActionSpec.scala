package common.helper

import common.domain.{Email, Password, UserCredentials}
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.http.HeaderNames._
import play.api.mvc.Results._
import play.api.mvc.{AnyContentAsEmpty, AnyContent}
import play.api.test.{FakeHeaders, Helpers, FakeRequest}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SecuredActionSpec extends Specification {

  "SecuredAction" should {
    "redirectHttps must return moved directly request with https domain" in new SecuredActionContext {
      Await.result(SecuredAction.redirectHttps(httpRequest), Duration.Inf).toString() must
        beEqualTo(MovedPermanently("https:///users").toString())
    }

    "getCredentialsFromAuthHeader must return correct UserCredentials" in new SecuredActionContext {
      SecuredAction.getCredentialsFromAuthHeader("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==") must
        beEqualTo(Option(UserCredentials(Email("Aladdin"), Password("open sesame"))))
    }

    "getCredentialsFromAuthHeader must return none for invalid header" in new SecuredActionContext {
      SecuredAction.getCredentialsFromAuthHeader("QWxhZGRpbjpvcGVuIHNlc2FtZQ==") must
        beEqualTo(None)
    }

    "requestAuthorization must return result with WWW_Authenticat header" in new SecuredActionContext {
      Await.result(SecuredAction.requestAuthorization, Duration.Inf).toString() must
        beEqualTo(Unauthorized.withHeaders(WWW_AUTHENTICATE -> """Basic realm="Secured"""").toString())
    }
  }

  trait SecuredActionContext extends Scope {
    val httpRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(),
      AnyContentAsEmpty)
  }
}

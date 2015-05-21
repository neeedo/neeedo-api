package common.helper

import common.domain._
import common.exceptions.Unauthorized
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsJson}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}

class ControllerUtilsSpec extends Specification {
  "ControllerUtils" should {
    "isAuthorized must return true for json without userId" in new ControllerUtilsContext {
      val securedRequest = new SecuredRequest(UserId("123"), fakeRequestWithoutUserId)
      utils.isAuthorized(withoutUserIdSecuredRequest) must beTrue
    }

    "isAuthorized must return false for json with wrong userId" in new ControllerUtilsContext {
      utils.isAuthorized(unmatchingSecuredRequest) must beFalse
    }

    "isAuthorized must return true for json with correct userId" in new ControllerUtilsContext {
      utils.isAuthorized(matchingSecuredRequest) must beTrue
    }

    "handleSecuredRequest must throw Unauthorized for unmatching userIds" in new ControllerUtilsContext {
      utils.handleSecuredRequest(unmatchingSecuredRequest, draft) must throwA[Unauthorized]
    }

    "handleSecuredRequest must return draft for matching userIds" in new ControllerUtilsContext {
      utils.handleSecuredRequest(matchingSecuredRequest, draft) must beEqualTo(draft)
    }

    "handleSecuredRequest must return draft for nonsecured request" in new ControllerUtilsContext {
      utils.handleSecuredRequest(fakeRequestWithUserId, draft) must beEqualTo(draft)
    }

    "bindRequestFromJson must throw exception for empty body" in new ControllerUtilsContext {
      val bindingTry = utils.bindRequestJsonBody(fakeRequestWithEmptyBody)(OfferDraft.offerDraftReads)

      bindingTry.failed.get.getMessage must beEqualTo("Missing body json object")
    }

    "bindRequestFromJson must throw exception for wrong json in body" in new ControllerUtilsContext {
      val bindingTry = utils.bindRequestJsonBody(fakeRequestWithBrokenBody)(OfferDraft.offerDraftReads)

      bindingTry.failed.get.getMessage must beEqualTo("Invalid json body")
    }

    "bindRequestFromJson must return draft for correct request" in new ControllerUtilsContext {
      val bindingTry = utils.bindRequestJsonBody(fakeRequestWithUserId)(OfferDraft.offerDraftReads)
      bindingTry.get must beEqualTo(draft)
    }
  }

  trait ControllerUtilsContext extends Scope {
    val utils = new ControllerUtils {}
    val userId = UserId("123")

    val draft = OfferDraft(
      userId,
      Set("Socken"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00)
    )

    val jsonWithUserId = Json.toJson(draft)
    val jsonWithoutUserId = Json.parse("""{ "error" : "blub" }""")

    val fakeRequestWithoutUserId = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
      AnyContentAsJson(jsonWithoutUserId),
      secure = true)

    val fakeRequestWithUserId = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
      AnyContentAsJson(jsonWithUserId),
      secure = true)

    val fakeRequestWithEmptyBody = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
      AnyContentAsEmpty,
      secure = true)

    val fakeRequestWithBrokenBody = new FakeRequest[AnyContent](
      Helpers.POST,
      "/users",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
      AnyContentAsJson(Json.parse("{}")),
      secure = true)

    val withoutUserIdSecuredRequest = new SecuredRequest(userId, fakeRequestWithoutUserId)
    val matchingSecuredRequest = new SecuredRequest(userId, fakeRequestWithUserId)
    val unmatchingSecuredRequest = new SecuredRequest(UserId("999"), fakeRequestWithUserId)
  }
}

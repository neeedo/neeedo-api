package controllers

import common.domain._
import common.helper.SecuredAction
import model.{DemandId, Demand, Card}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeHeaders, FakeRequest, Helpers, WithApplication}
import services.{UserService, MatchingService}

import scala.concurrent.Future

class MatchingControllerSpec extends Specification with Mockito {

  "Matching Controller.matchDemand" should {

    "return 400 for POST request with missing body" in new MatchingControllerContext {
      val fakeRequest = emptyBodyFakeRequest.withHeaders(("Content-Type","application/json"))
      val res: Future[Result] = ctrl.matchDemand(Some(Pager(0,0)))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }

    "return 400 for POST request with invalid Demand Json" in new MatchingControllerContext {
      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(wrongDemandJson)
      val res: Future[Result] = ctrl.matchDemand(Some(Pager(0,0)))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }


    "return 200 if MatchingService returns a MatchingResult" in new MatchingControllerContext {
      matchingService.matchDemand(any[Pager], any[Demand]) returns
        Future.successful(List.empty)

      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(demand))
      val res: Future[Result] = ctrl.matchDemand(Some(Pager(20, 0)))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo("{\"offers\":[]}")
    }
  }

  trait MatchingControllerContext extends WithApplication {
    val matchingService = mock[MatchingService]
    val userService = mock[UserService]
    userService.authorizeUser(any[UserCredentials]) returns Future(Some(UserId("abc")))
    val securedAction = new SecuredAction(userService)
    val ctrl = new MatchingController(matchingService, securedAction)

    val emptyBodyFakeRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Basic dGVzdDp0ZXN0"))),
      AnyContentAsEmpty,
      secure = true)

    val demand = Demand(
      DemandId("123"),
      Version(1),
      UserId("abc"),
      Set("Socken", "Bekleidung"),
      Set("Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Distance(100),
      Price(0.00),
      Price(10.00)
    )

    val wrongDemandJson: JsObject = Json.obj(
      "id" -> demand.id.value,
      "userId" -> demand.uid.value,
      "mustTags" -> demand.mustTags,
      "location" -> Json.obj(
        "lon" -> demand.location.lon.value
      ),
      "distance" -> demand.distance.value,
      "price" -> Json.obj(
        "max" -> demand.priceMax.value
      )
    )
  }
}

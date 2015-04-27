package controllers

import common.domain.{MatchingResult, PageSize, From}
import model.Card
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContent, Result}
import play.api.test.{FakeHeaders, Helpers, FakeRequest}
import services.MatchingService
import play.api.test.Helpers.defaultAwaitTimeout
import test.{TestApplications, TestData}


import scala.concurrent.Future

class MatchingSpec extends Specification with Mockito {

  val emptyBodyFakeRequest = new FakeRequest[AnyContent](
    Helpers.POST,
    "/",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
    AnyContentAsEmpty,
    secure = true)

  "Matching Controller.matchDemand" should {

    "return 400 for POST request with missing body" in TestApplications.loggingOffApp() {
      val matchingService = mock[MatchingService]
      val ctrl = new Matching(matchingService)
      val fakeRequest = emptyBodyFakeRequest.withHeaders(("Content-Type","application/json"))
      val res: Future[Result] = ctrl.matchDemand(From(0), PageSize(0))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "return 400 for POST request with invalid Demand Json" in TestApplications.loggingOffApp() {
      val matchingService = mock[MatchingService]
      val ctrl = new Matching(matchingService)
      val demandJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle"}""")
      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(demandJson)
      val res: Future[Result] = ctrl.matchDemand(From(0), PageSize(0))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }


    "return 200 if MatchingService returns a MatchingResult" in TestApplications.loggingOffApp() {
      val matchingService = mock[MatchingService]
      val from = From(0)
      val pageSize = PageSize(0)
      matchingService.matchDemand(from, pageSize, TestData.demand) returns
        Future.successful(MatchingResult(0, from, pageSize, List.empty[Card]))

      val ctrl = new Matching(matchingService)
      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(TestData.demandJson)
      val res: Future[Result] = ctrl.matchDemand(from, pageSize)(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo("{\"matches\":{\"total\":0,\"from\":0,\"pageSize\":0,\"matching\":[]}}")
    }
  }
}

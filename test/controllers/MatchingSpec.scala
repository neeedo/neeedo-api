package controllers

import common.domain.{MatchingResult, PageSize, From}
import model.Card
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{Helpers, FakeRequest}
import services.MatchingService
import play.api.test.Helpers.defaultAwaitTimeout
import test.TestData


import scala.concurrent.Future

class MatchingSpec extends Specification with Mockito {
  "Matching" should {

    "matchDemand must return 400 cannot parse json for post requests with invalid demand json" in {
      val matchingService = mock[MatchingService]
      val ctrl = new Matching(matchingService)
      val demandJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle"}""")
      val fakeRequest = FakeRequest(Helpers.POST, "/matching/0/0")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(demandJson)
      val res: Future[Result] = ctrl.matchDemand(From(0), PageSize(0))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "matchDemand must return 400 Missing Body for post requests without body" in {
      val matchingService = mock[MatchingService]
      val ctrl = new Matching(matchingService)
      val fakeRequest = FakeRequest(Helpers.POST, "/matching/0/0")
        .withHeaders(("Content-Type","application/json"))
      val res: Future[Result] = ctrl.matchDemand(From(0), PageSize(0))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "matchDemand must return 200 when matching service returns a MatchingResult" in {
      val matchingService = mock[MatchingService]
      val from = From(0)
      val pageSize = PageSize(0)
      matchingService.matchDemand(from, pageSize, TestData.demand) returns
        Future.successful(MatchingResult(0, from, pageSize, List.empty[Card]))

      val ctrl = new Matching(matchingService)
      val fakeRequest = FakeRequest(Helpers.POST, "/matching/0/0")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(TestData.demandJson)
      val res: Future[Result] = ctrl.matchDemand(from, pageSize)(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo("{\"matches\":{\"total\":0,\"from\":0,\"pageSize\":0,\"matching\":[]}}")
    }
  }
}

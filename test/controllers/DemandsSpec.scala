package controllers


import common.domain._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import services.DemandService
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.Future

class DemandsSpec extends Specification with Mockito {
  "Demands Controller" should {
    "createDemands must return 400 missing body for post requests without body" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val res: Future[Result] = ctrl.createDemand()(FakeRequest(Helpers.POST, "/"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }
  }

  "createDemands must return 400 cannot parse json for post requests with invalid demanddraft" in {
    val demandService = mock[DemandService]
    val ctrl = new Demands(demandService)
    val demandDraftJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212},"distance":30,"price":{"min":25.0,"max":77.0}}""")
    val fakeHeaders = FakeHeaders(Seq(("Content-Type", Seq("application/json"))))
    val fakeRequest = FakeRequest(Helpers.POST, "/")
      .withHeaders(("Content-Type","application/json"))
      .withJsonBody(demandDraftJson)
    val res: Future[Result] = ctrl.createDemand()(fakeRequest)

    Helpers.status(res) must equalTo(400)
    Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
  }
}

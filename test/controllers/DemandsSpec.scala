package controllers


import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers
import services.DemandService
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.Future

class DemandsSpec extends Specification with Mockito {
  "Demands Controller" should {
    "createDemands must return 400 missing body for post requests without body" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val res: Future[Result] = ctrl.createDemand()(FakeRequest("POST", "/"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }
  }
}

package common.domain

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication
import test.TestData

class DemandDraftSpec extends Specification {

  "DemandDraft" should {
    "json should be correctly parsed into an object" in new WithApplication {
      TestData.demandDraftJson.as[DemandDraft] must beEqualTo(TestData.demandDraft)
    }

    "object should be correctly parsed into json" in new WithApplication {
      Json.toJson(TestData.demandDraft) must beEqualTo(TestData.demandDraftJson)
    }
  }
}

package common.domain

import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication
import test.TestData

class OfferDraftSpec extends Specification {

  "OfferDraft" should {
    "json should be correctly parsed into an object" in new WithApplication {
      TestData.offerDraftJson.as[OfferDraft] must beEqualTo(TestData.offerDraft)
    }

    "tag list must be trimmed" in new WithApplication {
      TestData.offerDraftJsonWithWhitespaces.as[OfferDraft] must beEqualTo(TestData.offerDraft)
    }

    "object should be correctly parsed into json" in new WithApplication {
      Json.toJson(TestData.offerDraft) must beEqualTo(TestData.offerDraftJson)
    }
  }
}

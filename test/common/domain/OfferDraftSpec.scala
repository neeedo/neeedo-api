package common.domain

import org.specs2.mutable.Specification
import play.api.libs.json.{Json, JsObject}
import play.api.test.WithApplication

class OfferDraftSpec extends Specification {

  val offerDraftJs: JsObject = Json.obj(
    "userId" -> "testUid",
    "tags" -> "testTags",
    "location" -> Json.obj(
      "lat" -> 10.0,
      "lon" -> 20.0
    ),
    "price" -> 100.0
  )

  val offerDraft = OfferDraft(
    UserId("testUid"),
    "testTags",
    Location(Longitude(20.0), Latitude(10.0)),
    Price(100.0))

  "OfferDraft" should {
    "json should be correctly parsed into an object" in new WithApplication {
      offerDraftJs.as[OfferDraft] must beEqualTo(offerDraft)
    }

    "object should be correctly parsed into json" in new WithApplication {
      Json.toJson(offerDraft) must beEqualTo(offerDraftJs)
    }
  }
}

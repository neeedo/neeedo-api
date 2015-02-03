package common.domain

import org.specs2.mutable.Specification
import play.api.libs.json.{Json, JsObject}
import play.api.test.WithApplication

class DemandDraftSpec extends Specification {

  val demandDraftJs: JsObject = Json.obj(
    "userId" -> "testUid",
    "mustTags" -> Json.arr("testTag1", "testTag2"),
    "shouldTags" -> Json.arr("testTag1", "testTag2"),
    "location" -> Json.obj(
      "lat" -> 10.0,
      "lon" -> 20.0
    ),
    "distance" -> 1,
    "price" -> Json.obj(
      "min" -> 50.0,
      "max" -> 100.0
    )
  )

  val demandDraft: DemandDraft = DemandDraft(
    UserId("testUid"),
    Set("testTag1", "testTag2"),
    Set("testTag1", "testTag2"),
    Location(Longitude(20.0), Latitude(10.0)),
    Distance(1),
    Price(50.0),
    Price(100.0))

  "DemandDraft" should {
    "json should be correctly parsed into an object" in new WithApplication {
      demandDraftJs.as[DemandDraft] must beEqualTo(demandDraft)
    }

    "object should be correctly parsed into json" in new WithApplication {
      Json.toJson(demandDraft) must beEqualTo(demandDraftJs)
    }
  }
}

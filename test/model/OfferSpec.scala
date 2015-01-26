package model

import common.domain._
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.test.WithApplication

class OfferSpec extends Specification {

  val offerJs: JsObject = Json.obj(
    "id" -> "testId",
    "version" -> 1L,
    "userId" -> "testUid",
    "tags" -> "testTags",
    "location" -> Json.obj(
      "lat" -> 10.0,
      "lon" -> 20.0
    ),
    "price" -> 100.0
  )

  val offer: Offer = Offer(
    OfferId("testId"),
    Version(1L),
    UserId("testUid"),
    "testTags",
    Location(Longitude(20.0), Latitude(10.0)),
    Price(100.0))

  "Offer" should {
    "offer json should be correctly parsed into a offer object" in new WithApplication {
      offerJs.as[Offer] must beEqualTo(offer)
    }

    "offer object should be correctly parsed into a offer json" in new WithApplication {
      Json.toJson(offer) must beEqualTo(offerJs)
    }
  }

  "OfferId" should {
    "be correctly be created from an identifier" in new WithApplication {
      OfferId.pathBinder.bind("key1", "12345abc") mustEqual Right(OfferId("12345abc"))
    }

    "be correctly be transform into an identifier" in new WithApplication {
      OfferId.pathBinder.unbind("key", OfferId("12345abc")) mustEqual("12345abc")
    }
  }
}

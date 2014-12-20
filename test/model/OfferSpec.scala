package model

import common.domain._
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.test.WithApplication

class OfferSpec extends Specification {

  val offerJs: JsObject = Json.obj(
    "id" -> "testId",
    "userId" -> "testUid",
    "productId" -> "testPid",
    "tags" -> "testTags",
    "location" -> Json.obj(
      "lat" -> 10.0,
      "lon" -> 20.0
    ),
    "price" -> 100.0
  )

  val offer: Offer = Offer(OfferId("testId"),
    UserId("testUid"),
    ProductId("testPid"),
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
}

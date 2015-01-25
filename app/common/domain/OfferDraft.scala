package common.domain

import model.{Offer, ProductId}
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.mvc.PathBindable

case class OfferDraft(
                       uid: UserId,
                       pid: ProductId,
                       tags: String,
                       location: Location,
                       price: Price)

object OfferDraft {

  implicit val offerReads: Reads[OfferDraft] = (
      (JsPath \ "userId").read[String] and
      (JsPath \ "productId").read[String] and
      (JsPath \ "tags").read[String] and
      (JsPath \ "location" \ "lat").read[Double] and
      (JsPath \ "location" \ "lon").read[Double] and
      (JsPath \ "price").read[Double]
    ) {
    (uid, pid, tags, lat, lon, price) => OfferDraft(UserId(uid), ProductId(pid), tags, Location(Longitude(lon), Latitude(lat)),
      Price(price))
  }

  implicit val offerWrites = new Writes[OfferDraft] {
    def writes(o: OfferDraft) = Json.obj(
      "userId" -> o.uid.value,
      "productId" -> o.pid.value,
      "tags" -> o.tags,
      "location" -> Json.obj(
        "lat" -> o.location.lat.value,
        "lon" -> o.location.lon.value
      ),
      "price" -> o.price.value
    )
  }
}

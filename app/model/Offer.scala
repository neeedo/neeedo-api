package model

import common.domain._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class OfferId(value: String)
case class Offer(
  id: OfferId,
  uid: UserId,
  pid: ProductId,
  tags: String,
  location: Location,
  price: Price)

object Offer {

  implicit val offerReads: Reads[Offer] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "userId").read[String] and
    (JsPath \ "productId").read[String] and
    (JsPath \ "tags").read[String] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double]
    ) {
    (id, uid, pid, tags, lat, lon, price) => Offer(OfferId(id), UserId(uid), ProductId(pid), tags, Location(Longitude(lon), Latitude(lat)),
      Price(price))
  }

  implicit val offerWrites = new Writes[Offer] {
    def writes(o: Offer) = Json.obj(
      "id" -> o.id.value,
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
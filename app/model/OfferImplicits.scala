package model

import common.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait OfferImplicits {

  implicit val offerReads: Reads[Offer] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "userId").read[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double]
    ) {
    (id, version, uid, tags, lat, lon, price) =>
      Offer(
        OfferId(id),
        Version(version),
        UserId(uid),
        tags.map(x => x.trim).filter(_ != ""),
        Location(
          Longitude(lon),
          Latitude(lat)
        ),
        Price(price)
      )
  }

  implicit val offerWrites = new Writes[Offer] {
    def writes(o: Offer) = Json.obj(
      "id" -> o.id.value,
      "version" -> o.version.value,
      "userId" -> o.uid.value,
      "tags" -> o.tags,
      "location" -> Json.obj(
        "lat" -> o.location.lat.value,
        "lon" -> o.location.lon.value
      ),
      "price" -> o.price.value
    )
  }

}

package model

import common.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait OfferImplicits {

  implicit val offerReads: Reads[Offer] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "user" \ "id").read[String] and
    (JsPath \ "user" \ "name").read[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double] and
    (JsPath \ "images").read[Set[String]]
    ) {
    (id, version, uid, uname, tags, lat, lon, price, images) =>
      Offer(
        OfferId(id),
        Version(version),
        UserIdAndName(
          UserId(uid),
          Username(uname)
        ),
        tags.map(x => x.trim).filter(_ != ""),
        Location(
          Longitude(lon),
          Latitude(lat)
        ),
        Price(price),
        images.map(x => x.trim).filter(_ != "")
      )
  }

  implicit val offerWrites = new Writes[Offer] {
    def writes(o: Offer) = Json.obj(
      "id" -> o.id.value,
      "version" -> o.version.value,
      "user" -> Json.obj(
        "id" -> o.user.id.value,
        "name" -> o.user.name.value
      ),
      "tags" -> o.tags,
      "location" -> Json.obj(
        "lat" -> o.location.lat.value,
        "lon" -> o.location.lon.value
      ),
      "price" -> o.price.value,
      "images" ->o.images
    )
  }

}

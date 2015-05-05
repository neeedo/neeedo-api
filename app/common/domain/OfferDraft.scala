package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class OfferDraft(
  uid: UserId,
  tags: Set[String],
  location: Location,
  price: Price)

object OfferDraft {

  def generateName(offerDraft: OfferDraft) = "Biete: " + offerDraft.tags.mkString(" ")

  implicit val offerDraftReads: Reads[OfferDraft] = (
    (JsPath \ "userId").read[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double]
  ) {
    (uid, tags, lat, lon, price) =>
      OfferDraft(
        UserId(uid),
        tags.map(x => x.trim).filter(!_.equals("")),
        Location(
          Longitude(lon),
          Latitude(lat)
        ),
        Price(price)
      )
  }

  implicit val offerDraftWrites = new Writes[OfferDraft] {
    def writes(o: OfferDraft) = Json.obj(
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

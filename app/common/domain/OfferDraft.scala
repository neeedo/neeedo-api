package common.domain

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class OfferDraft(
  uid: UserId,
  tags: Set[String],
  location: Location,
  price: Price,
  images: Set[String])

object OfferDraft {

  def generateName(offerDraft: OfferDraft) = s"Biete: ${offerDraft.tags.mkString(" ")} ${UUID.randomUUID()}"

  implicit val offerDraftReads: Reads[OfferDraft] = (
    (JsPath \ "userId").read[String] and
    (JsPath \ "tags").read[Set[String]] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double] and
    (JsPath \ "images").read[Set[String]]
  ) {
    (uid, tags, lat, lon, price, images) =>
      OfferDraft(
        UserId(uid),
        tags.map(x => x.trim).filterNot(_.equals("")),
        Location(
          Longitude(lon),
          Latitude(lat)
        ),
        Price(price),
        images.map(x => x.trim).filterNot(_.equals(""))
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
      "price" -> o.price.value,
      "images" ->o.images
    )
  }
}

package common.domain

import java.util.UUID

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DemandDraft(
	uid: UserId,
	mustTags: Set[String],
  shouldTags: Set[String],
	location: Location,
	distance: Distance,
	priceMin: Price,
	priceMax: Price)

object DemandDraft {

	def generateName(demandDraft: DemandDraft) = s"Suche: ${demandDraft.mustTags.mkString(" ")} ${UUID.randomUUID()}"

	implicit val demandReads: Reads[DemandDraft] = (
		(JsPath \ "userId").read[String] and
		(JsPath \ "mustTags").read[Set[String]] and
		(JsPath \ "shouldTags").read[Set[String]] and
		(JsPath \ "location" \ "lat").read[Double] and
		(JsPath \ "location" \ "lon").read[Double] and
		(JsPath \ "distance").read[Int] and
		(JsPath \ "price" \ "min").read[Double] and
		(JsPath \ "price" \ "max").read[Double]
		) {
		(uid, mustTags, shouldTags, lat, lon, distance, priceMin, priceMax) =>
			DemandDraft(
        UserId(uid),
        mustTags.map(x => x.trim).filter(!_.equals("")),
        shouldTags.map(x => x.trim).filter(!_.equals("")),
        Location( Longitude(lon), Latitude(lat) ),
        Distance(distance),
        Price(priceMin),
        Price(priceMax)
      )
		}

	implicit val demandWrites = new Writes[DemandDraft] {
		def writes(d: DemandDraft) = Json.obj(
		  "userId" -> d.uid.value,
		  "mustTags" -> d.mustTags,
		  "shouldTags" -> d.shouldTags,
		  "location" -> Json.obj(
			  "lat" -> d.location.lat.value,
			  "lon" -> d.location.lon.value
			),
		  "distance" -> d.distance.value,
		  "price" -> Json.obj(
			  "min" -> d.priceMin.value,
			  "max" -> d.priceMax.value
			)
		)
	}
}

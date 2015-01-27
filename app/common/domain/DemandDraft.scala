package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class DemandDraft(
	uid: UserId,
	tags: String,
	location: Location,
	distance: Distance,
	//TODO pricerange case class?
	priceMin: Price,
	priceMax: Price)

object DemandDraft {

	implicit val demandReads: Reads[DemandDraft] = (
		(JsPath \ "userId").read[String] and
		(JsPath \ "tags").read[String] and
		(JsPath \ "location" \ "lat").read[Double] and
		(JsPath \ "location" \ "lon").read[Double] and
		(JsPath \ "distance").read[Int] and
		(JsPath \ "price" \ "min").read[Double] and
		(JsPath \ "price" \ "max").read[Double]
		) {
		(uid, tags, lat, lon, distance, priceMin, priceMax) => DemandDraft(UserId(uid), tags, Location(Longitude(lon), Latitude(lat)),
		Distance(distance), Price(priceMin), Price(priceMax))
		}

	implicit val demandWrites = new Writes[DemandDraft] {
		def writes(d: DemandDraft) = Json.obj(
		  "userId" -> d.uid.value,
		  "tags" -> d.tags,
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

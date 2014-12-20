package model

import common.domain._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class DemandId(value: String)
case class Demand(
	id: DemandId,
	uid: UserId,
	tags: String,
	location: Location,
	distance: Distance,
	priceMin: Price,
	priceMax: Price)

object Demand {

	implicit val demandReads: Reads[Demand] = (
		(JsPath \ "id").read[String] and
		(JsPath \ "userId").read[String] and
		(JsPath \ "tags").read[String] and
		(JsPath \ "location" \ "lat").read[Double] and
		(JsPath \ "location" \ "lon").read[Double] and
		(JsPath \ "distance").read[Int] and
			(JsPath \ "price" \ "min").read[Double] and
			(JsPath \ "price" \ "max").read[Double]
		) {
		(id, uid, tags, lat, lon, distance, priceMin, priceMax) => Demand(DemandId(id), UserId(uid), tags, Location(Longitude(lon), Latitude(lat)),
		Distance(distance), Price(priceMin), Price(priceMax))
		}

	implicit val demandWrites = new Writes[Demand] {
		def writes(d: Demand) = Json.obj(
			"id" -> d.id.value,
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
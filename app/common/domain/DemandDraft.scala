package common.domain

import play.api.libs.functional.syntax._
import play.api.libs.json._

import scala.util.Random

case class DemandDraft(
	uid: UserId,
	mustTags: Set[String],
  shouldTags: Set[String],
	location: Location,
	distance: Distance,
	//TODO pricerange case class?
	priceMin: Price,
	priceMax: Price)

object DemandDraft {

	implicit val demandReads: Reads[DemandDraft] = (
		(JsPath \ "userId").read[String] and
		(JsPath \ "mustTags").read[Set[String]] and
		(JsPath \ "mustTags").read[Set[String]] and
		(JsPath \ "location" \ "lat").read[Double] and
		(JsPath \ "location" \ "lon").read[Double] and
		(JsPath \ "distance").read[Int] and
		(JsPath \ "price" \ "min").read[Double] and
		(JsPath \ "price" \ "max").read[Double]
		) {
		(uid, mustTags, shouldTags, lat, lon, distance, priceMin, priceMax) =>
			DemandDraft(
        UserId(uid),
        mustTags,
        shouldTags,
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

  def generateName(demandDraft: DemandDraft) = "Suche: " + demandDraft.mustTags.mkString(" ")
}

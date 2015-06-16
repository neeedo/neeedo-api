package model

import common.domain._
import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Writes, JsPath, Reads}

trait DemandImplicits {

  implicit val demandReads: Reads[Demand] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "user" \ "id").read[String] and
    (JsPath \ "user" \ "name").read[String] and
    (JsPath \ "mustTags").read[Set[String]] and
    (JsPath \ "shouldTags").read[Set[String]] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "distance").read[Int] and
    (JsPath \ "price" \ "min").read[Double] and
    (JsPath \ "price" \ "max").read[Double]
    ) {
    (id, version, uid, username, mustTags, shouldTags, lat, lon, distance, priceMin, priceMax) =>
      Demand(
        DemandId(id),
        Version(version),
        UserId(uid),
        Username(username),
        mustTags.map(x => x.trim).filter(_ != ""),
        shouldTags.map(x => x.trim).filter(_ != ""),
        Location( Longitude(lon), Latitude(lat) ),
        Distance(distance),
        Price(priceMin),
        Price(priceMax)
      )
  }

  implicit val demandWrites = new Writes[Demand] {
    def writes(d: Demand) = Json.obj(
      "id" -> d.id.value,
      "version" -> d.version.value,
      "user" -> Json.obj(
        "id" -> d.uid.value,
        "name" -> d.uname.value
      ),
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

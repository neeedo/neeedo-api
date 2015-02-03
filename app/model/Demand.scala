package model

import common.domain._
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.Product
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.PathBindable

case class Demand(
	id: DemandId,
	version: Version,
	uid: UserId,
	mustTags: Set[String],
	shouldTags: Set[String],
	location: Location,
	distance: Distance,
	//TODO pricerange case class?
	priceMin: Price,
	priceMax: Price) {
}

object Demand extends ModelUtils {

	implicit val demandReads: Reads[Demand] = (
		(JsPath \ "id").read[String] and
		(JsPath \ "version").read[Long] and
		(JsPath \ "userId").read[String] and
		(JsPath \ "mustTags").read[Set[String]] and
		(JsPath \ "shouldTags").read[Set[String]] and
		(JsPath \ "location" \ "lat").read[Double] and
		(JsPath \ "location" \ "lon").read[Double] and
		(JsPath \ "distance").read[Int] and
		(JsPath \ "price" \ "min").read[Double] and
		(JsPath \ "price" \ "max").read[Double]
		) {
		(id, version, uid, mustTags, shouldTags, lat, lon, distance, priceMin, priceMax) =>
      Demand(
        DemandId(id),
        Version(version),
        UserId(uid),
        mustTags,
        shouldTags,
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

  def productToDemand(product: Product): Option[Demand] = {
    try {
      Some(
        Demand(
          DemandId(product.getId),
          Version(product.getVersion),
          UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
          getAttribute(product, "mustTags").getValue(AttributeAccess.ofString().attributeMapper()).split(";").toSet,
          getAttribute(product, "shouldTags").getValue(AttributeAccess.ofString().attributeMapper()).split(";").toSet,
          Location(
            Longitude(getAttribute(product, "longitude").getValue(AttributeAccess.ofDouble().attributeMapper())),
            Latitude(getAttribute(product, "latitude").getValue(AttributeAccess.ofDouble().attributeMapper()))
          ),
          Distance(getAttribute(product, "distance").getValue(AttributeAccess.ofDouble().attributeMapper()).intValue()),
          // Todo Nullpointer case
          Price(getAttribute(product, "priceMin").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue()),
          Price(getAttribute(product, "priceMax").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue())
        )
      )
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to parse product as a valid Demand.")
        None
    }
  }
}

case class DemandId(value: String)
object DemandId {
	implicit def pathBinder: PathBindable[DemandId] = new PathBindable[DemandId] {
		override def bind(key: String, value: String): Either[String, DemandId] = {
			DemandId.apply(value) match {
				case x: DemandId => Right(x)
				//TODO id validation here!
				case _	=> Left("Bla")
			}
		}
		override def unbind(key: String, demandId: DemandId): String = demandId.value
	}
}

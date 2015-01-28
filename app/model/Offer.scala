package model

import common.domain._
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.Product
import play.api.Logger
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.PathBindable

case class Offer(
  id: OfferId,
  version: Version,
  uid: UserId,
  tags: String,
  location: Location,
  price: Price)

object Offer extends ModelUtils {

  implicit val offerReads: Reads[Offer] = (
    (JsPath \ "id").read[String] and
    (JsPath \ "version").read[Long] and
    (JsPath \ "userId").read[String] and
    (JsPath \ "tags").read[String] and
    (JsPath \ "location" \ "lat").read[Double] and
    (JsPath \ "location" \ "lon").read[Double] and
    (JsPath \ "price").read[Double]
    ) {
    (id, version, uid, tags, lat, lon, price) => Offer(OfferId(id), Version(version), UserId(uid), tags, Location(Longitude(lon), Latitude(lat)),
      Price(price))
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

  def productToOffer(product: Product): Option[Offer] = {
    try {
      Some(
        Offer(
          OfferId(product.getId),
          Version(product.getVersion),
          UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
          getAttribute(product, "tags").getValue(AttributeAccess.ofString().attributeMapper()),
          Location(
            Longitude(getAttribute(product, "longitude").getValue(AttributeAccess.ofDouble().attributeMapper())),
            Latitude(getAttribute(product, "latitude").getValue(AttributeAccess.ofDouble().attributeMapper()))
          ),
          // Todo Nullpointer case
          Price(getAttribute(product, "price").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue())
        )
      )
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to parse product as a valid Offer.")
        None
    }
  }
}

case class OfferId(value: String)
object OfferId {
  implicit def pathBinder: PathBindable[OfferId] = new PathBindable[OfferId] {
    override def bind(key: String, value: String): Either[String, OfferId] = {
      OfferId.apply(value) match {
        case x: OfferId => Right(x)
        //TODO id validation here!
        case _	=> Left("Bla")
      }
    }
    override def unbind(key: String, offerId: OfferId): String = offerId.value
  }
}

package model

import common.domain._
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.Product
import play.api.Logger
import play.api.libs.json.Writes

sealed trait Card

object Card {
  implicit val cardWriter = Writes[Card] {
    case d: Demand => Demand.demandWrites.writes(d)
    case o: Offer => Offer.offerWrites.writes(o)
  }
}


case class Demand(
  id: DemandId,
  version: Version,
  uid: UserId,
  mustTags: Set[String],
  shouldTags: Set[String],
  location: Location,
  distance: Distance,
  priceMin: Price,
  priceMax: Price) extends Card

object Demand extends ModelUtils with DemandImplicits {

  def fromProduct(product: Product): Option[Demand] = {
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


case class Offer(
  id: OfferId,
  version: Version,
  uid: UserId,
  tags: Set[String],
  location: Location,
  price: Price) extends Card

object Offer extends ModelUtils with OfferImplicits {

  def fromProduct(product: Product): Option[Offer] = {
    try {
      Some(
        Offer(
          OfferId(product.getId),
          Version(product.getVersion),
          UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
          getAttribute(product, "tags").getValue(AttributeAccess.ofString().attributeMapper()).split(";").toSet,
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

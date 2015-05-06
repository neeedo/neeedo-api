package model

import common.domain._
import io.sphere.sdk.products.Product
import play.api.Logger

sealed trait Card

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
      Some(Demand(
          DemandId(product.getId),
          Version(product.getVersion),
          UserId(readStringAttribute(product, "userId")),
          readStringAttribute(product, "mustTags").split(";").toSet,
          readStringAttribute(product, "shouldTags").split(";").toSet,
          Location(
            Longitude(readDoubleAttribute(product, "longitude")),
            Latitude(readDoubleAttribute(product, "latitude"))
          ),
          Distance(readDoubleAttribute(product, "distance").intValue()),
          Price(readMoneyAttribute(product, "priceMin").getNumber.doubleValue()),
          Price(readMoneyAttribute(product, "priceMax").getNumber.doubleValue())
      ))
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
          UserId(readStringAttribute(product, "userId")),
          readStringAttribute(product, "tags").split(";").toSet,
          Location(
            Longitude(readDoubleAttribute(product, "longitude")),
            Latitude(readDoubleAttribute(product, "latitude"))
          ),
          Price(readMoneyAttribute(product, "price").getNumber.doubleValue())
        )
      )
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to parse product as a valid Offer.")
        None
    }
  }
}

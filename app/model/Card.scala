package model

import common.domain._
import io.sphere.sdk.products.Product
import play.api.Logger


import scala.util.Try

sealed trait Card

case class Demand(id: DemandId, version: Version, uid: UserId, mustTags: Set[String], shouldTags: Set[String],
                  location: Location, distance: Distance, priceMin: Price, priceMax: Price) extends Card

object Demand extends ModelUtils with DemandImplicits {

  def fromProduct(product: Product): Option[Demand] = {
    val variant = product.getMasterData.getStaged.getMasterVariant
    try {
      Some(Demand(
          DemandId(product.getId),
          Version(product.getVersion),
          UserId(readStringAttribute(variant, "userId")),
          readStringAttribute(variant, "mustTags").split(";").toSet,
          readStringAttribute(variant, "shouldTags").split(";").toSet,
          Location(
            Longitude(readDoubleAttribute(variant, "longitude")),
            Latitude(readDoubleAttribute(variant, "latitude"))
          ),
          Distance(readDoubleAttribute(variant, "distance").intValue()),
          Price(readMoneyAttribute(variant, "priceMin").getNumber.doubleValue()),
          Price(readMoneyAttribute(variant, "priceMax").getNumber.doubleValue())
      ))
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to parse product as a valid Demand.")
        None
    }
  }

}


case class Offer(id: OfferId, version: Version, uid: UserId, tags: Set[String],
                 location: Location, price: Price, images: Set[String]) extends Card

object Offer extends ModelUtils with OfferImplicits {

  def fromProduct(product: Product): Try[Offer] = {
    val variant = product.getMasterData.getStaged.getMasterVariant
    Try {
      Offer(
        OfferId(product.getId),
        Version(product.getVersion),
        UserId(readStringAttribute(variant, "userId")),
        readStringAttribute(variant, "tags").split(";").toSet,
        Location(
          Longitude(readDoubleAttribute(variant, "longitude")),
          Latitude(readDoubleAttribute(variant, "latitude"))
        ),
        Price(readMoneyAttribute(variant, "price").getNumber.doubleValue()),
        readStringSetAttribute(variant, "images")
      )
    }
  }
}

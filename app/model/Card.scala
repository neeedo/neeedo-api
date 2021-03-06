package model

import common.domain._
import io.sphere.sdk.products.Product

import scala.util.Try

sealed trait Card

case class Demand(id: DemandId, version: Version, user: UserIdAndName, mustTags: Set[String], shouldTags: Set[String],
                  location: Location, distance: Distance, priceMin: Price, priceMax: Price) extends Card

object Demand extends ModelUtils with DemandImplicits {

  def fromProduct(product: Product): Try[Demand] = {
    val variant = product.getMasterData.getStaged.getMasterVariant
    Try {
      Demand(
        DemandId(product.getId),
        Version(product.getVersion),
        UserIdAndName(
          UserId(readStringAttribute(variant, "userId")),
          Username(readStringAttribute(variant, "userName"))
        ),
        readStringSetAttribute(variant, "mustTags"),
        readStringSetAttribute(variant, "shouldTags"),
        Location(
          Longitude(readDoubleAttribute(variant, "longitude")),
          Latitude(readDoubleAttribute(variant, "latitude"))
        ),
        Distance(readDoubleAttribute(variant, "distance").intValue()),
        Price(readMoneyAttribute(variant, "priceMin").getNumber.doubleValue()),
        Price(readMoneyAttribute(variant, "priceMax").getNumber.doubleValue())
      )
    }
  }

}


case class Offer(id: OfferId, version: Version, user: UserIdAndName, tags: Set[String],
                 location: Location, price: Price, images: Set[String]) extends Card

object Offer extends ModelUtils with OfferImplicits {

  def fromProduct(product: Product): Try[Offer] = {
    val variant = product.getMasterData.getStaged.getMasterVariant
    Try {
      Offer(
        OfferId(product.getId),
        Version(product.getVersion),
        UserIdAndName(
          UserId(readStringAttribute(variant, "userId")),
          Username(readStringAttribute(variant, "userName"))
        ),
        readStringSetAttribute(variant, "tags"),
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

package model

import common.domain._
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
      Some(Demand(
          DemandId(product.getId),
          Version(product.getVersion),
          UserId(readStringAttribute("userId")),
          readStringAttribute("mustTags").split(";").toSet,
          readStringAttribute("shouldTags").split(";").toSet,
          Location(
            Longitude(readDoubleAttribute("longitude")),
            Latitude(readDoubleAttribute("latitude"))
          ),
          Distance(readDoubleAttribute("distance").intValue()),
          Price(readMoneyAttribute("priceMin").getNumber.doubleValue()),
          Price(readMoneyAttribute("priceMax").getNumber.doubleValue())
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
          UserId(readStringAttribute("userId")),
          readStringAttribute("tags").split(";").toSet,
          Location(
            Longitude(readDoubleAttribute("longitude")),
            Latitude(readDoubleAttribute("latitude"))
          ),
          Price(readMoneyAttribute("price").getNumber.doubleValue())
        )
      )
    } catch {
      case e: Exception =>
        Logger.error(s"Failed to parse product as a valid Offer.")
        None
    }
  }
}

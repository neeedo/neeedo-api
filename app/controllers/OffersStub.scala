package controllers

import common.domain._
import model.{OfferId, Offer, ProductId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}


class OffersStub extends Controller {
  val offerDraft1 = OfferDraft(UserId("1"), "smartphone neuwertig iphone", Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))

  val offer1 = Offer(OfferId("1"), Version(1L), UserId("1"), "computer PC service reparatur", Location(Longitude(52.468562), Latitude(13.534212)), Price(20.0))
  val offer2 = Offer(OfferId("2"), Version(1L), UserId("2"), "comics sammlung avengers", Location(Longitude(52.468562), Latitude(13.534212)), Price(500.0))
  val offer3 = Offer(OfferId("3"), Version(2L), UserId("3"), "bekleidung handmade gestrickt mütze hipster", Location(Longitude(52.468562), Latitude(13.534212)), Price(5.0))

  def fetchOffer(offerId: OfferId): Option[Offer] = offerId.value match {
    case "1" => Some(offer1)
    case "2" => Some(offer2)
    case "3" => Some(offer3)
    case _   => None
  }

  def createOffer = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[OfferDraft] match {
          case Some(offerDraft) => Created(Json.obj("demand" -> Json.toJson(offerDraft)))
          case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
        }
      case None => BadRequest(Json.obj("error" -> "Missing body"))
    }
  }

  def listOffers = Action {
    val offers = offer1 :: offer2 :: offer3 :: Nil
    Ok(Json.obj("offers" -> Json.toJson(offers)))
  }

  def getOffer(offerId: OfferId) = Action {
    fetchOffer(offerId) match {
      case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
      case None => NotFound(Json.obj("error" -> "Offers Entity not found"))
    }
  }

  def updateOffer(offerId: OfferId) = Action { implicit request =>
    fetchOffer(offerId) match {
      case Some(offer) =>
        request.body.asJson match {
          case Some(json) =>
            json.asOpt[Offer] match {
              case Some(_offer) => Ok
              case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
            }
          case None => BadRequest(Json.obj("error" -> "Missing body"))
        }
      case None => NotFound(Json.obj("error" -> "Offer Entity not found"))
    }
  }

  def deleteOffer(offerId: OfferId) = Action {
    fetchOffer(offerId) match {
      case Some(offer) => Ok
      case None => NotFound(Json.obj("error" -> "Offer Entity not found"))
    }
  }
}

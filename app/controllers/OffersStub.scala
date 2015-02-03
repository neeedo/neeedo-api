package controllers

import common.domain._
import model._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}


class OffersStub extends Controller {
  val offerDraft1 = OfferDraft(UserId("1"), Set("smartphone", "neuwertig", "iphone"), Location(Longitude(52.468562), Latitude(13.534212)), Price(299.95))

  val offer1 = Offer(OfferId("1"), Version(1L), UserId("1"), Set("computer", "PC", "service", "reparatur"), Location(Longitude(52.468562), Latitude(13.534212)), Price(20.0))
  val offer2 = Offer(OfferId("2"), Version(1L), UserId("2"), Set("comics", "sammlung", "avengers"), Location(Longitude(52.468562), Latitude(13.534212)), Price(500.0))
  val offer3 = Offer(OfferId("3"), Version(2L), UserId("3"), Set("bekleidung", "handmade", "gestrickt", "mütze", "hipster"), Location(Longitude(52.468562), Latitude(13.534212)), Price(5.0))

  def fetchOffer(id: OfferId): Option[Offer] = id.value match {
    case "1" => Some(offer1)
    case "2" => Some(offer2)
    case "3" => Some(offer3)
    case _   => None
  }

  def fetchOffer(id: OfferId, version: Version): Option[Offer] = (id.value, version.value) match {
    case ("1", 1L) => Some(offer1)
    case ("2", 1L) => Some(offer2)
    case ("3", 2L) => Some(offer3)
    case _ => None
  }

  def createOffer = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[OfferDraft] match {
          case Some(_) => Created(Json.obj("offer" -> Json.toJson(offer1)))
          case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
        }
      case None => BadRequest(Json.obj("error" -> "Missing body"))
    }
  }

  // TODO move to matching controller
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

  def updateOffer(id: OfferId, version: Version) = Action { implicit request =>
    fetchOffer(id, version) match {
      case Some(_) =>
        request.body.asJson match {
          case Some(json) =>
            json.asOpt[OfferDraft] match {
              case Some(draft) => {
//                val updatedOffer = Offer(id, version ++, draft.uid, draft.tags, draft.location, draft.price)
//                Ok(Json.obj("offer" -> Json.toJson(updatedOffer)))
                Ok(Json.obj("offer" -> Json.toJson(draft)))
              }
              case None => BadRequest(Json.obj("error" -> "Cannot parse json"))
            }
          case None => BadRequest(Json.obj("error" -> "Missing body"))
        }
      case None => NotFound(Json.obj("error" -> "Offer Entity not found"))
    }
  }

  def deleteOffer(id: OfferId, version: Version) = Action {
    fetchOffer(id, version) match {
      case Some(_) => Ok
      case None => NotFound(Json.obj("error" -> "Offer Entity not found"))
    }
  }
}

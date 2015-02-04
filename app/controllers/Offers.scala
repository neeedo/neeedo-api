package controllers

import common.domain._
import model.OfferId
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.OfferService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Offers(service: OfferService) extends Controller {

  def createOffer = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        json.asOpt[OfferDraft] match {
          case Some(draft) => service.createOffer(draft).map {
            case Some(offer) => Created(Json.obj("offer" -> Json.toJson(offer)))
            case _ => BadRequest(Json.obj("error" -> "Unknown error"))
          }
          case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
        }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def getOffer(id: OfferId) = Action.async {
    service.getOfferById(id).map {
      case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
      case None => NotFound(Json.obj("error" -> "Offer Entity not found"))
    }
  }

  def updateOffer(id: OfferId, version: Version) = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.asOpt[OfferDraft] match {
        case Some(draft) => service.updateOffer(id, version, draft).map {
          case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
          case _ => BadRequest(Json.obj("error" -> "Unknown error"))
        }
        case None => Future.successful(BadRequest(Json.obj("error" -> "Cannot parse json")))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> "Missing body")))
    }
  }

  def deleteOffer(id: OfferId, version: Version) = Action.async {
    service.deleteOffer(id, version).map {
      case Some(_) => Ok
      case None => NotFound
    }
  }

 }

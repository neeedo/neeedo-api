package controllers

import common.domain._
import common.helper.{ControllerUtils, SecuredAction}
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.OfferService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}
import common.helper.ImplicitConversions.ExceptionToResultConverter

class OffersController(service: OfferService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  def createOffer = securedAction.async { implicit request =>
    val offerDraft = bindRequestJsonBody(request)(OfferDraft.offerDraftReads)

    offerDraft match {
      case Success(draft) => service.createOffer(draft) map {
        offer => Created(Json.obj("offer" -> Json.toJson(offer)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

  def getOfferById(id: OfferId) = securedAction.async {
    service.getOfferById(id) map {
      case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
      case None => NotFound(Json.obj("error" -> "Offer not found"))
    }
  }

  def getOffersByUserId(id: UserId) = securedAction.async {
    service.getOffersByUserId(id) map { offers: List[Offer] =>
      Ok(Json.obj("offers" -> Json.toJson(offers)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def getAllOffers = Action.async {
    service.getAllOffers map { offers: List[Offer] =>
      Ok(Json.obj("offers" -> Json.toJson(offers)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def updateOffer(id: OfferId, version: Version) = securedAction.async { implicit request =>
    val offerDraft = bindRequestJsonBody(request)(OfferDraft.offerDraftReads)

    offerDraft match {
      case Success(o) =>
        service.updateOffer(id, version, o) map {
          offer => Ok(Json.obj("offer" -> Json.toJson(offer)))
        } recover { case e: Exception => e.asResult }
      case Failure(e) => Future(e.asResult)
    }
  }


  def deleteOffer(id: OfferId, version: Version) = securedAction.async {
    service.deleteOffer(id, version)
      .map(_ => Ok)
      .recover {
        case e: Exception => e.asResult
      }
  }

  def deleteAllOffers() = securedAction.async {
    service.deleteAllOffers()
      .map(_ => Ok)
      .recover { case e: Exception => e.asResult }
  }
}

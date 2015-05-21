package controllers

import common.domain._
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.{ControllerUtils, SecuredAction}
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.Controller
import services.OfferService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

class OffersController(service: OfferService) extends Controller with ControllerUtils {

  def createOffer = SecuredAction.async { implicit request =>
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

  def getOffer(id: OfferId) = SecuredAction.async {
    service.getOfferById(id).map {
      case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
      case None => NotFound(Json.obj("error" -> "Offer not found"))
    }
  }

  def getOffersByUserId(id: UserId) = SecuredAction.async {
    service.getOffersByUserId(id).map { offers: List[Offer] =>
      Ok(Json.obj("offers" -> Json.toJson(offers)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def updateOffer(id: OfferId, version: Version) = SecuredAction.async { implicit request =>
    val offerDraft = bindRequestJsonBody(request)(OfferDraft.offerDraftReads)

    offerDraft match {
      case Success(o) =>
        service.updateOffer(id, version, o) map {
          offer => Ok(Json.obj("offer" -> Json.toJson(offer)))
        } recover { case e: Exception => e.asResult }
      case Failure(e) => Future(e.asResult)
    }
  }


  def deleteOffer(id: OfferId, version: Version) = SecuredAction.async {
    service.deleteOffer(id, version)
      .map(_ => Ok)
      .recover {
        case e: Exception => e.asResult
      }
  }
}

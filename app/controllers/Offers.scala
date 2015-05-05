package controllers

import common.domain._
import common.helper.ControllerUtils._
import common.helper.ImplicitConversions.ExceptionToResultConverter
import common.helper.SecuredAction
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.OfferService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

class Offers(service: OfferService) extends Controller {

  def createOffer = SecuredAction.async { implicit request =>
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

  def updateOffer(id: OfferId, version: Version) = SecuredAction.async { implicit request =>
    val offerDraft = bindRequestJsonBody(request.body)(OfferDraft.offerDraftReads)

    offerDraft match {
      case Success(o) =>
        service.updateOffer(id, version, o) map {
         offer => Ok(Json.obj("offer" -> Json.toJson(offer)))
      } recover { case e: Exception => e.asResult }

      case Failure(e) => Future(e.asResult)
    }
  }

  def deleteOffer(id: OfferId, version: Version) = SecuredAction.async {
    service.deleteOffer(id, version) map {
      case Success(_) => Ok
      case Failure(e) => e.asResult
    }
  }

  def addImageToOffer(id: OfferId) = Action.async { implicit request =>
    val externalImage = bindRequestJsonBody(request.body)(ExternalImage.externalImageReads)

    externalImage match {
      case Success(img) =>
        service.addImageToOffer(id, img) map {
          offer => Created(Json.obj("offer" -> Json.toJson(offer)))
        } recover { case e: Exception => e.asResult }

      case Failure(e) => Future(e.asResult)
    }
  }

 }

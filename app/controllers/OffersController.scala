package controllers

import common.domain._
import common.helper.{ControllerUtils, SecuredAction}
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.OfferService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Random, Success, Failure}
import common.helper.ImplicitConversions.ExceptionToResultConverter

class OffersController(service: OfferService, securedAction: SecuredAction) extends Controller with ControllerUtils {

  val pagerOffsetDefault = 0
  val pagerLimitDefault = 20

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

  def createTestOffer = Action.async { implicit request =>
    val offerDraft = bindRequestJsonBody(request)(OfferDraft.offerDraftReads)

    offerDraft match {
      case Success(draft) =>
        val latitude = Random.nextDouble() * (52.675499 - 52.338120) + 52.338120
        val longitude = Random.nextDouble() * (13.761340 - 13.088400) + 13.088400
        val location = Location(Longitude(longitude), Latitude(latitude))
        val testDraft = draft.copy(uid = UserId("e4d755ed-1f95-4b67-8589-ee5001ae1759"), location = location)
        service.createOffer(testDraft) map {
        offer => Created(Json.obj("offer" -> Json.toJson(offer)))
      } recover {
        case e: Exception => e.asResult
      }
      case Failure(e) => Future(e.asResult)
    }
  }

  def getOfferById(id: OfferId) = Action.async {
    service.getOfferById(id) map {
      case Some(offer) => Ok(Json.obj("offer" -> Json.toJson(offer)))
      case None => NotFound(Json.obj("error" -> "Offer not found"))
    }
  }

  def getOffersByUserId(id: UserId, pagerOption: Option[Pager]) = securedAction.async {
    val pager = pagerOption.getOrElse(Pager(pagerLimitDefault, pagerOffsetDefault))

    service.getOffersByUserId(id, pager) map { offers: List[Offer] =>
      Ok(Json.obj("offers" -> Json.toJson(offers)))
    } recover {
      case e: Exception => e.asResult
    }
  }

  def getAllOffers(p: Option[Pager], locOption: Option[Location]) = Action.async {
    val pager = p.getOrElse(Pager(20, 0))
    service.getAllOffers(pager, locOption) map { offers: List[Offer] =>
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

package controllers

import common.domain._
import common.helper.{ControllerUtils, SecuredAction}
import common.helper.ImplicitConversions.ExceptionToResultConverter
import model.{Offer, OfferId}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import services.{ImageService, OfferService}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Random, Success, Failure}

class OffersController(service: OfferService, imageService: ImageService, securedAction: SecuredAction) extends Controller with ControllerUtils {

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
    val berlinHTW = Location(Longitude(13.525885), Latitude(52.456540))
    val berlinCenter = Location(Longitude(13.404880), Latitude(52.519242))

    offerDraft match {
      case Success(draft) =>
        val location = service.randomLocation(berlinCenter, Distance(15))
        val images = draft.images.map(url => Await.result(imageService.createImage(url), 10 seconds).value)

        val testDraft = draft.copy(location = location, images = images)

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

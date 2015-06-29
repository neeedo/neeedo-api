package services

import common.domain._
import common.logger.OfferLogger
import model.{Offer, OfferId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.es.{EsMessageService, EsOfferService}
import services.sphere.SphereOfferService

import scala.concurrent.Future

class OfferService(sphereOfferService: SphereOfferService, esOfferService: EsOfferService, messageService: EsMessageService) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    sphereOfferService.createOffer(draft).flatMap {
      offer =>
        esOfferService.createOffer(offer).flatMap {
          res =>
            messageService.alertDemandsFor(res.id)
              .map(_ => res)
              .recover { case e: Exception => res }
        }.recoverWith {
          case e: Exception =>
            sphereOfferService.deleteOffer(offer.id, offer.version)
            throw e
        }
    }
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    sphereOfferService.getOfferById(id)
  }

  def getOffersByUserId(id: UserId, pager: Pager): Future[List[Offer]] = {
    esOfferService.getOffersByUserId(id, pager)
  }

  def getAllOffers(pager: Pager, loc: Option[Location]): Future[List[Offer]] = {
    esOfferService.getAllOffers(pager, loc)
  }

  def updateOffer(id: OfferId, version: Version, draft: OfferDraft): Future[Offer] = {
    createOffer(draft) flatMap {
      offer => deleteOffer(id, version) map(_ => offer)
    }
  }

  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    esOfferService.deleteOffer(id).flatMap {
      offerId => sphereOfferService.deleteOffer(offerId, version)
    }
  }

  def deleteAllOffers(): Future[Any] = {
    esOfferService.deleteAllOffers()
    sphereOfferService.deleteAllOffers()
  }
}



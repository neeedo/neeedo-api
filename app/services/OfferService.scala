package services

import common.domain._
import model.{Offer, OfferId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.es.EsOfferService
import services.sphere.SphereOfferService

import scala.concurrent.Future

class OfferService(sphereOfferService: SphereOfferService, esOfferService: EsOfferService) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    sphereOfferService.createOffer(draft).flatMap {
      offer =>
        esOfferService.createOffer(offer).recoverWith {
          case e: Exception =>
            sphereOfferService.deleteOffer(offer.id, offer.version)
            throw e
        }
    }
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    sphereOfferService.getOfferById(id)
  }

  def getOffersByUserId(id: UserId): Future[List[Offer]] = {
    esOfferService.getOffersByUserId(id)
  }

  def getAllOffers: Future[List[Offer]] = {
    esOfferService.getAllOffers
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



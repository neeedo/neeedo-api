package services

import common.domain._
import model.{Offer, OfferId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.es.EsOfferService
import services.sphere.SphereOfferService

import scala.concurrent.Future

class OfferService(sphereOfferService: SphereOfferService, esOfferService: EsOfferService) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    sphereOfferService.writeOfferToSphere(draft).flatMap {
      offer => esOfferService.writeOfferToEs(offer).recoverWith {
        case e: Exception =>
          deleteOffer(offer.id, offer.version)
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

  def updateOffer(id: OfferId, version: Version, draft: OfferDraft): Future[Offer] = {
    createOffer(draft) flatMap {
      offer => deleteOffer(id, version)
    }
  }

  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    esOfferService.deleteOfferFromElasticsearch(id).flatMap {
      offerId => sphereOfferService.deleteOffer(offerId, version)
    } recover {
      // TODO Log and Throw
      case e: Exception => throw e
    }
  }


  def addImageToOffer(id: OfferId, img: ExternalImage): Future[Offer] = {
    addImageToOffer(id, img) flatMap {
      offer => esOfferService.addImageToOffer(offer.id, img)
    } recover {
      case e: Exception => throw e
    }
  }
}



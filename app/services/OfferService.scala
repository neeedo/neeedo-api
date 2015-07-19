package services

import common.domain._
import common.logger.OfferLogger
import model.{Offer, OfferId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import services.es.{EsMessageService, EsOfferService}
import services.sphere.SphereOfferService

import scala.concurrent.Future
import scala.util.Random

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

  def randomLocation(location: Location, maxDistance: Distance) = {
    def lonKmToKmDeg(dist: Double, lat: Latitude) = dist / (111000 * Math.cos(lat.value))
    def latKmToDeg(dist: Double) = dist / 111000

    val random = new Random()
    val angleRad = random.nextInt(360) * Math.PI * 2 / 360
    val distance = random.nextInt(maxDistance.value * 1000)

    val dlon = lonKmToKmDeg(Math.cos(angleRad) * distance, location.lat)
    val dlat = latKmToDeg(Math.sin(angleRad) * distance)

    val lon = location.lon.value + dlon
    val lat = location.lat.value + dlat

    Location(Longitude(lon), Latitude(lat))
  }
}



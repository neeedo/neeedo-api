package services

import common.domain.{Favorite, UserId}
import common.sphere.ProductTypes
import model.Offer
import services.es.EsFavoriteService
import services.sphere.SphereOfferService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FavoriteService(esService: EsFavoriteService, sphereService: SphereOfferService) {

  def addFavorite(favorite: Favorite) = esService.addFavorite(favorite)

  def getFavoritesByUser(userId: UserId) = {
    esService.getFavoritesByUser(userId) flatMap {
      favorites =>
        if(favorites.isEmpty)
          Future(List.empty[Offer])
        else sphereService.getOffersByIds(favorites map (f => f.offerId))
    }
  }

  def removeFavorite(favorite: Favorite) = esService.removeFavorite(favorite)

}
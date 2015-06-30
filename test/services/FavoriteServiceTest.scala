package services

import common.domain._
import common.helper.{UUIDHelper, ConfigLoader}
import model.{Offer, OfferId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.test.WithApplication
import services.es.EsFavoriteService
import services.sphere.SphereOfferService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class FavoriteServiceTest extends Specification with Mockito {

  trait FavoriteServiceContext extends WithApplication {
    val uuid = new UUIDHelper

    val config = Map("config keys here" -> "value")
    val configLoader = new ConfigLoader(Configuration.from(config))

    val sphereService = mock[SphereOfferService]
    val esService = mock[EsFavoriteService]

    val favoriteService = new FavoriteService(esService, sphereService)

    val offerId = OfferId(uuid.random)
    val userId = UserId(uuid.random)
    val offer = Offer(offerId, Version(1L), UserIdAndName(userId, Username("test")),
      Set("abc", "efg"), Location(Longitude(1), Latitude(1)), Price(1), Set())

    val favorite = Favorite(userId, offerId)
  }

  "FavoriteService.getFavoritesByUser" should {

    "return List[Offer] if favorites exist" in new FavoriteServiceContext {
      esService.getFavoritesByUser(userId) returns Future(List(favorite))
      sphereService.getOffersByIds(List(offerId)) returns Future(List(offer))

      val res = Await.result(favoriteService.getFavoritesByUser(userId), Duration(1, "second"))

      res mustEqual List(offer)
    }

    "return empty List[Offer] if no favorites exist" in new FavoriteServiceContext {
      esService.getFavoritesByUser(userId) returns Future(List.empty[Favorite])

      val res = Await.result(favoriteService.getFavoritesByUser(userId), Duration(1, "second"))

      res mustEqual List.empty[Offer]
      there was no (sphereService).getOffersByIds(List.empty[OfferId])
    }

  }
}

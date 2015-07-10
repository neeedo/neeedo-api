package controllers

import common.domain._
import common.helper.SecuredAction
import model.{Offer, OfferId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeHeaders, FakeRequest, Helpers, WithApplication}
import services.FavoriteService
import services.sphere.SphereUserService
import test.TestData

import scala.concurrent.Future

class FavoritesControllerSpec extends Specification with Mockito {

  trait FavoritesControllerContext extends WithApplication {
    val favoritesService = mock[FavoriteService]
    val userService = mock[SphereUserService]
    val securedAction = new SecuredAction(userService)
    val controller = new FavoritesController(favoritesService, securedAction)

    val uid = UserId("testUser")

    val offer1 = Offer(
      OfferId("o1"),
      Version(1L),
      UserIdAndName(
        uid,
        Username("name")
      ),
      Set("test", "tags"),
      Location(
        Longitude(50),
        Latitude(13)
      ),
      Price(10),
      Set.empty[String]
    )

    val offer2 = Offer(
      OfferId("o2"),
      Version(1L),
      UserIdAndName(
        uid,
        Username("name")
      ),
      Set("other", "tags"),
      Location(
        Longitude(50),
        Latitude(13)
      ),
      Price(10),
      Set.empty[String]
    )

    val favorite = Favorite(uid, offer1.id)
    val favoriteJson = Json.toJson(favorite)

    val getFavoritesFakeRequest = new FakeRequest[AnyContent](
      Helpers.GET,
      "/favorites",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    val addFavoriteFakeRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/favorites",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true).withJsonBody(favoriteJson)

    val removeFavoriteFakeRequest = new FakeRequest[AnyContent](
      Helpers.DELETE,
      "/favorites",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true).withJsonBody(favoriteJson)

    userService.authorizeUser(any[UserCredentials]) returns Future(Option(uid))
  }

  "FavoritesController" should {

    "getFavoritesByUser should return 200 and a list of Offers as " +
      "json body" in new FavoritesControllerContext {
      val favorites = List(offer1, offer2)
      favoritesService.getFavoritesByUser(any[UserId]) returns Future(favorites)

      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(favorites)).toString())
    }

    "addFavorite should return 201 and given favorite as json" in new FavoritesControllerContext {
      favoritesService.addFavorite(any[Favorite]) returns Future(favorite)

      val res = controller.addFavorite()(addFavoriteFakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> Json.toJson(favorite)).toString())
    }

    "removeFavorites should return 200 and given a valid favorite id" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[Favorite]) returns Future(true)

      val res: Future[Result] = controller.removeFavorite(uid, offer1.id)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "removeFavorites should return 404 and given a wrong favorite id" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[Favorite]) returns Future(false)

      val res: Future[Result] = controller.removeFavorite(uid, offer1.id)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

  }
}

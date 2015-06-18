package controllers

import common.domain.{UserCredentials, UserId}
import common.helper.SecuredAction
import model.OfferId
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, AnyContentAsEmpty, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeHeaders, FakeRequest, Helpers, WithApplication}
import services.UserService
import test.TestData

import scala.concurrent.Future

class FavoritesControllerSpec extends Specification with Mockito {

  trait FavoritesControllerContext extends WithApplication {
    val favoritesService = mock[FavoriteService]
    val userService = mock[UserService]
    val securedAction = new SecuredAction(userService)
    val controller = new FavoritesController(favoritesService, securedAction)

    val id1 = OfferId("o1")
    val id2 = OfferId("o2")
    val uid = UserId("testUser")


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
      secure = true)

    val removeFavoriteFakeRequest = new FakeRequest[AnyContent](
      Helpers.DELETE,
      "/favorites",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    userService.authorizeUser(any[UserCredentials]) returns Future(Option(uid))
  }

  "FavoritesController" should {

    "getFavoritesByUser should return 200 and a list of Cards as " +
      "json body" in new FavoritesControllerContext {
      val favorites = List(id1, id2)
      favoritesService.getFavoritesByUser(any[UserId]) returns Future(favorites)

      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(favorites map (_.value))).toString())
    }

    "getFavoritesByUser should return 200 and an empty list of OfferIds as " +
      "json body if userId is not found" in new FavoritesControllerContext {
      favoritesService.getFavoritesByUser(any[UserId]) returns Future(List.empty[OfferId])

      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(List.empty[String])).toString())
    }

    "addFavorite should return 201 and given OfferId, if OfferId isn't " +
      "already in favorites" in new FavoritesControllerContext {
      favoritesService.addFavorite(any[OfferId]) returns Future(Option(id1))

      val res: Future[Result] = controller.addFavorite(id1)(addFavoriteFakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
    }

    "addFavorites should return 200 and given OfferId, if OfferId is " +
      "already in favorites" in new FavoritesControllerContext {
      favoritesService.addFavorite(any[OfferId]) returns Future(Option.empty)

      val res: Future[Result] = controller.addFavorite(id1)(addFavoriteFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
    }

    "removeFavorites should return 200 and given OfferId if OfferId is " +
      "in favorites" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[OfferId]) returns Future(Option(id1))

      val res: Future[Result] = controller.removeFavorite(id1)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
    }

    "removeFavorites should return 404 if given OfferId is not in " +
      "favorites" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[OfferId]) returns Future(Option.empty)

      val res: Future[Result] = controller.removeFavorite(id1)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

  }
}

package controllers

import common.domain.{UserId, UserCredentials}
import common.helper.SecuredAction
import model.{Card, OfferId, CardId, DemandId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{Result, AnyContentAsEmpty, AnyContent}
import play.api.test.{FakeHeaders, Helpers, FakeRequest, WithApplication}
import play.api.test.Helpers.defaultAwaitTimeout
import services.{UserService, FavoritesService}
import test.TestData

import scala.concurrent.Future

class FavoritesControllerSpec extends Specification with Mockito {

  trait FavoritesControllerContext extends WithApplication {
    val favoritesService = mock[FavoritesService]
    val userService = mock[UserService]
    val securedAction = new SecuredAction(userService)
    val controller = new FavoritesController(favoritesService, securedAction)

    val id1 = DemandId("testDemand")
    val id2 = OfferId("testOffer")
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

    "getFavoritesByUser should return 200 and an empty list of CardIds as " +
      "json body if userId is not found" in new FavoritesControllerContext {
      favoritesService.getFavoritesByUser(any[UserId]) returns Future(List.empty[CardId])

      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(List.empty[String])).toString())
    }

    "addFavorite should return 201 and given CardId, if CardId isn't " +
      "already in favorites" in new FavoritesControllerContext {
      favoritesService.addFavorite(any[CardId]) returns Future(Option(id1))

      val res: Future[Result] = controller.addFavorite(id1)(addFavoriteFakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("id" -> id1.value).toString())
    }

    "addFavorites should return 200 and given CardId, if CardId is " +
      "already in favorites" in new FavoritesControllerContext {
      favoritesService.addFavorite(any[CardId]) returns Future(Option.empty)

      val res: Future[Result] = controller.addFavorite(id1)(addFavoriteFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("id" -> id1.value).toString())
    }

    "removeFavorites should return 200 and given CardId if CardId is " +
      "in favorites" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[CardId]) returns Future(Option(id1))

      val res: Future[Result] = controller.removeFavorite(id1)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("id" -> id1.value).toString())
    }

    "removeFavorites should return 404 if given CardId is not in " +
      "favorites" in new FavoritesControllerContext {
      favoritesService.removeFavorite(any[CardId]) returns Future(Option.empty)

      val res: Future[Result] = controller.removeFavorite(id1)(removeFavoriteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

  }
}

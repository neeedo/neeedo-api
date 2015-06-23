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
import services.{FavoriteService, UserService}
import test.TestData

import scala.concurrent.Future

class FavoritesControllerSpec extends Specification with Mockito {

  trait FavoritesControllerContext extends WithApplication {
    val favoritesService = mock[FavoriteService]
    val userService = mock[UserService]
    val securedAction = new SecuredAction(userService)
    val controller = new FavoritesController(favoritesService, securedAction)

    val uid = UserId("testUser")

    val id1 = OfferId("o1")
    val id2 = OfferId("o2")

    val offer1 = Offer(
      id1,
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
      id2,
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

    val favorite1 = Favorite(uid, offer1.id)
    val favorite2 = Favorite(uid, offer2.id)

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

//  "FavoritesController" should {
//
//    "getFavoritesByUser should return 200 and a list of Cards as " +
//      "json body" in new FavoritesControllerContext {
//      val favorites = List(offer1, offer2)
//      favoritesService.getFavoritesByUser(any[UserId]) returns Future(favorites)
//
//      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)
//
//      Helpers.status(res) must equalTo(200)
////      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(favorites map (_.value))).toString())
//    }
//
//    "getFavoritesByUser should return 200 and an empty list of OfferIds as " +
//      "json body if userId is not found" in new FavoritesControllerContext {
//      favoritesService.getFavoritesByUser(any[UserId]) returns Future(List.empty[Offer])
//
//      val res: Future[Result] = controller.getFavoritesByUser(uid)(getFavoritesFakeRequest)
//
//      Helpers.status(res) must equalTo(200)
////      Helpers.contentAsString(res) must equalTo(Json.obj("favorites" -> Json.toJson(List.empty[String])).toString())
//    }
//
//    "addFavorite should return 201 and given OfferId, if OfferId isn't " +
//      "already in favorites" in new FavoritesControllerContext {
//      favoritesService.addFavorite(favorite1) returns Future(favorite1)
//
//      val res: Future[Result] = controller.addFavorite()(addFavoriteFakeRequest)
//
//      Helpers.status(res) must equalTo(201)
////      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
//    }
//
//    "addFavorites should return 200 and given OfferId, if OfferId is " +
//      "already in favorites" in new FavoritesControllerContext {
//      favoritesService.addFavorite(uid, any[OfferId]) returns Future(Option.empty)
//
//      val res: Future[Result] = controller.addFavorite(uid, id1)(addFavoriteFakeRequest)
//
//      Helpers.status(res) must equalTo(200)
////      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
//    }
//
//    "removeFavorites should return 200 and given OfferId if OfferId is " +
//      "in favorites" in new FavoritesControllerContext {
//      favoritesService.removeFavorite(uid, any[OfferId]) returns Future(id1)
//
//      val res: Future[Result] = controller.removeFavorite(uid, id1)(removeFavoriteFakeRequest)
//
//      Helpers.status(res) must equalTo(200)
////      Helpers.contentAsString(res) must equalTo(Json.obj("favorite" -> id1.value).toString())
//    }
//
////    "removeFavorites should return 404 if given OfferId is not in " +
////      "favorites" in new FavoritesControllerContext {
////      favoritesService.removeFavorite(uid, any[OfferId]) returns Future(Option.empty)
////
////      val res: Future[Result] = controller.removeFavorite(uid, id1)(removeFavoriteFakeRequest)
////
////      Helpers.status(res) must equalTo(404)
////    }
//
//  }
}

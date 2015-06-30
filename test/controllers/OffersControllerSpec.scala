package controllers

import common.domain._
import common.exceptions.{ElasticSearchIndexFailed, ProductNotFound}
import common.helper.SecuredAction
import model.{Offer, OfferId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContent, AnyContentAsEmpty, AnyContentAsJson, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeHeaders, FakeRequest, Helpers, WithApplication}
import services.OfferService
import services.sphere.SphereUserService
import test.TestData

import scala.concurrent.Future

class OffersControllerSpec extends Specification with Mockito {
  "Offers Controller" should {
//    "offer controller must return 401 for wrong user credentials in secured actions" in new OffersControllerContext {
//      val create: Future[Result] = ctrl.createOffer()(emptyBodyRequestWithWrongCredentials)
//      val delete: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)
//      val update: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)
//
//      Helpers.status(create) must equalTo(401)
//      Helpers.status(delete) must equalTo(401)
//      Helpers.status(update) must equalTo(401)
//    }
//
//    "offer controller must return 301 for non https request in secured actions" in new OffersControllerContext {
//      val create: Future[Result] = ctrl.createOffer()(emptyBodyRequestWithoutSsl)
//      val delete: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithoutSsl)
//      val update: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithoutSsl)
//
//      Helpers.status(create) must equalTo(301)
//      Helpers.status(delete) must equalTo(301)
//      Helpers.status(update) must equalTo(301)
//    }
//
//    "createOffer must return 400 missing body for post requests without body" in new OffersControllerContext {
//      val res: Future[Result] = ctrl.createOffer()(emptyBodyCreateFakeRequest)
//
//      Helpers.status(res) must equalTo(400)
//      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
//    }
//
//    "createOffer must return 400 cannot parse json for post requests with invalid offerdraft" in new OffersControllerContext {
//      val fakeRequest = emptyBodyCreateFakeRequest
//        .withHeaders(("Content-Type","application/json"))
//        .withJsonBody(wrongOfferDraftJson)
//      val res: Future[Result] = ctrl.createOffer()(fakeRequest)
//
//      Helpers.status(res) must equalTo(400)
//      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
//    }

//    "createOffer must return InternalServerError when offerService returns ElasticSearchFailedException" in new OffersControllerContext {
//      offerService.createOffer(any[OfferDraft]) returns Future.failed(new ElasticSearchIndexFailed("bla"))
//      val fakeRequest = emptyBodyCreateFakeRequest
//        .withJsonBody(Json.toJson(offerDraft))
//      val res: Future[Result] = ctrl.createOffer()(fakeRequest)
//
//      Helpers.status(res) must equalTo(500)
//      Helpers.contentAsString(res) must equalTo("{\"error\":\"bla\"}")
//    }

    "createOffer must return 201 when offerService returns offer" in new OffersControllerContext {
      offerService.createOffer(any[OfferDraft]) returns Future.successful(offer)

      val fakeRequest = emptyBodyCreateFakeRequest
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 200 and the offer json for a valid id" in new OffersControllerContext {
      offerService.getOfferById(any[OfferId]) returns Future.successful(Option(offer))

      val res: Future[Result] = ctrl.getOfferById(OfferId("1"))(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 404 and error json for a invalid id" in new OffersControllerContext {
      offerService.getOfferById(any[OfferId]) returns Future.successful(Option.empty[Offer])

      val res: Future[Result] = ctrl.getOfferById(OfferId("1"))(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Offer not found").toString())
    }

    "deleteOffer must return 200 for a valid id and version" in new OffersControllerContext {
      offerService.deleteOffer(any[OfferId], any[Version]) returns Future.successful(offer)

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "deleteOffer must return 404 for an invalid id or version" in new OffersControllerContext {
      offerService.deleteOffer(any[OfferId], any[Version]) returns Future.failed(new ProductNotFound("bla"))

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "bla").toString())
    }

    "deleteOffer must return 400 missing body for put requests without body" in new OffersControllerContext {
      val res: Future[Result] = ctrl.updateOffer(offer.id, offer.version)(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }

    "updateOffers must return 400 cannot parse json for put requests with invalid offer draft" in new OffersControllerContext {
      val fakeRequest = emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(wrongOfferDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }

    "updateOffers must return 500 internal server error when offerService returns elasticsearchIndexException" in new OffersControllerContext {
      offerService.updateOffer(any[OfferId], any[Version], any[OfferDraft]) returns
        Future.failed(new ElasticSearchIndexFailed("bla"))
      val fakeRequest = emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(offerDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"bla\"}")
    }

    "updateOffers must return 200 when offerService returns offer" in new OffersControllerContext {
      offerService.updateOffer(any[OfferId], any[Version], any[OfferDraft]) returns
        Future.successful(offer)

      val fakeRequest = emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(offerDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }
  }

  trait OffersControllerContext extends WithApplication {
    val offerService = mock[OfferService]
    val userService = mock[SphereUserService]
    userService.authorizeUser(any[UserCredentials]) returns Future(Some(UserId("abc")))
    val securedAction = new SecuredAction(userService)
    val ctrl = new OffersController(offerService, securedAction)

    val emptyBodyRequestWithWrongCredentials = new FakeRequest[AnyContent](
      Helpers.POST,
      "/offer",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
      AnyContentAsEmpty,
      secure = true)

    val emptyBodyRequestWithoutSsl = new FakeRequest[AnyContent](
      Helpers.POST,
      "/offer",
      FakeHeaders(),
      AnyContentAsEmpty,
      secure = false)

    val emptyBodyCreateFakeRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/offer",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true).withHeaders(("Content-Type","application/json"))

    val emptyBodyDeleteFakeRequest = new FakeRequest[AnyContent](
      Helpers.DELETE,
      "/offer/1/1",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    val emptyBodyUpdateFakeRequest = new FakeRequest[AnyContent](
      Helpers.PUT,
      "/offer/1/1",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken), "Content-Type" -> Seq("application/json"))),
      AnyContentAsEmpty,
      secure = true)

    val emptyBodyGetFakeRequest = new FakeRequest[AnyContent](
      Helpers.GET,
      "offer/1",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)
  }

  val offerDraft = OfferDraft(
    UserId("abc"),
    Set("Socken Wolle"),
    Location(Longitude(12.2), Latitude(15.5)),
    Price(50.00),
    Set.empty
  )

  val offer = Offer(
    OfferId("123"),
    Version(1),
    UserIdAndName(
      UserId("abc"),
      Username("test")
    ),
    Set("Socken Wolle"),
    Location(Longitude(12.2), Latitude(15.5)),
    Price(50.00),
    Set("xyz.jpg")
  )

  val wrongOfferDraftJson: JsObject = Json.obj(
    "userId" -> offer.user.id.value,
    "tags" -> "testTags",
    "location" -> Json.obj(
      "lon" -> 20.0
    ),
    "price" -> 100.0
  )

  val offerDraftJson: JsObject = Json.obj(
    "userId" -> offer.user.id.value,
    "tags" -> offer.tags,
    "location" -> Json.obj(
      "lat" -> offer.location.lat.value,
      "lon" -> offer.location.lon.value
    ),
    "price" -> offer.price.value,
    "images" -> offer.images
  )

}

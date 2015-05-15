package controllers

import common.domain._
import common.exceptions.{ProductNotFound, ElasticSearchIndexFailed}
import model.{OfferId, Offer}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsJson, AnyContentAsEmpty, AnyContent, Result}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{Helpers, FakeHeaders, FakeRequest}
import services.OfferService
import test.{TestData, TestApplications}

import scala.concurrent.Future

class OffersSpec extends Specification with Mockito {
  "Offers Controller" should {

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
      secure = true)

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

    "offer controller must return 401 for wrong user credentials in secured actions" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)

      val create: Future[Result] = ctrl.createOffer()(emptyBodyRequestWithWrongCredentials)
      val delete: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)
      val update: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)

      Helpers.status(create) must equalTo(401)
      Helpers.status(delete) must equalTo(401)
      Helpers.status(update) must equalTo(401)
    }

    "offer controller must return 301 for non https request in secured actions" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)

      val create: Future[Result] = ctrl.createOffer()(emptyBodyRequestWithoutSsl)
      val delete: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithoutSsl)
      val update: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(emptyBodyRequestWithoutSsl)

      Helpers.status(create) must equalTo(301)
      Helpers.status(delete) must equalTo(301)
      Helpers.status(update) must equalTo(301)
    }

    "createOffer must return 400 missing body for post requests without body" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)

      val res: Future[Result] = ctrl.createOffer()(emptyBodyCreateFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "createOffer must return 400 cannot parse json for post requests with invalid offerdraft" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraftJson: JsObject = Json.obj(
        "userId" -> "testUid",
        "tags" -> "testTags",
        "location" -> Json.obj(
          "lon" -> 20.0
        ),
        "price" -> 100.0
      )
      val fakeRequest = emptyBodyCreateFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(offerDraftJson)
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "createOffer must return InternalServerError offerService returns ElasticSearchFailedException" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      offerService.createOffer(offerDraft) returns Future.failed(new ElasticSearchIndexFailed("bla"))
      val fakeRequest = emptyBodyCreateFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"bla\"}")
    }

    "createOffer must return 200 when offerService returns offer" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      val offer = TestData.offer
      offerService.createOffer(offerDraft) returns Future.successful(offer)

      val fakeRequest = emptyBodyCreateFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 200 and the offer json for a valid id" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offer = TestData.offer
      offerService.getOfferById(OfferId("1")) returns Future.successful(Option(offer))

      val res: Future[Result] = ctrl.getOffer(OfferId("1"))(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 404 and error json for a invalid id" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.getOfferById(OfferId("1")) returns Future.successful(Option.empty[Offer])

      val res: Future[Result] = ctrl.getOffer(OfferId("1"))(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Offer not found").toString())
    }

    "deleteOffer must return 200 for a valid id and version" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offer = TestData.offer
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.successful(offer)

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "deleteOffer must return 404 for an invalid id or version" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.failed(new ProductNotFound("bla"))

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "bla").toString())
    }

    "deleteOffer must return 400 missing body for put requests without body" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "updateOffers must return 400 cannot parse json for put requests with invalid offer draft" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraftJson: JsObject = Json.obj(
        "userId" -> "testUid",
        "tags" -> "testTags",
        "location" -> Json.obj(
          "lon" -> 20.0
        ),
        "price" -> 100.0
      )
      val fakeRequest = emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(offerDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "updateOffers must return 500 internal server error when offerService returns elasticsearchIndexException" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.updateOffer(OfferId("1"), Version(1L), TestData.offerDraft) returns Future.failed(new ElasticSearchIndexFailed("bla"))
      val fakeRequest =  emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(TestData.offerDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"bla\"}")
    }

    "updateOffers must return 200 when offerService returns offer" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      val offer = TestData.offer
      offerService.updateOffer(OfferId("1"), Version(1L), offerDraft) returns Future.successful(offer)

      val fakeRequest = emptyBodyUpdateFakeRequest.withBody[AnyContent](AnyContentAsJson(TestData.offerDraftJson))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }
  }
}

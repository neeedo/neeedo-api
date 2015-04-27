package controllers

import common.domain._
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

    val emptyBodyFakeRequest = new FakeRequest[AnyContent](
      Helpers.POST,
      "/",
      FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
      AnyContentAsEmpty,
      secure = true)

    "createOffer must return 400 missing body for post requests without body" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)

      val res: Future[Result] = ctrl.createOffer()(emptyBodyFakeRequest)

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
      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(offerDraftJson)
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "createOffer must return 400 unknown error when offerService returns empty option" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      offerService.createOffer(offerDraft) returns Future.successful(Option.empty)
      val fakeRequest = emptyBodyFakeRequest
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createOffer must return 200 when offerService returns offer" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      val offer = TestData.offer
      offerService.createOffer(offerDraft) returns Future.successful(Option(offer))

      val fakeRequest = emptyBodyFakeRequest
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

      val res: Future[Result] = ctrl.getOffer(OfferId("1"))(FakeRequest())

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 404 and error json for a invalid id" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.getOfferById(OfferId("1")) returns Future.successful(Option.empty[Offer])

      val res: Future[Result] = ctrl.getOffer(OfferId("1"))(FakeRequest())

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Offer Entity not found").toString())
    }

    "deleteOffer must return 200 for a valid id and version" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val deleteFakeRequest = new FakeRequest[AnyContent](
        Helpers.DELETE,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
        AnyContentAsEmpty,
        secure = true)
      val offer = TestData.offer
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.successful(Option(offer))

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(deleteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "deleteOffer must return 404 for an invalid id or version" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.successful(Option.empty[Offer])
      val deleteFakeRequest = new FakeRequest[AnyContent](
        Helpers.DELETE,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
        AnyContentAsEmpty,
        secure = true)

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(deleteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

    "deleteOffer must return 400 missing body for put requests without body" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val deleteFakeRequest = new FakeRequest[AnyContent](
        Helpers.DELETE,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
        AnyContentAsEmpty,
        secure = true)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(deleteFakeRequest)

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
      val fakeRequest = new FakeRequest[AnyContent](
        Helpers.PUT,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken), "Content-Type" -> Seq("application/json"))),
        AnyContentAsJson(offerDraftJson),
        secure = true)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "updateOffers must return 400 unknown error when offerService returns empty option" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.updateOffer(OfferId("1"), Version(1L), TestData.offerDraft) returns Future.successful(Option.empty)
      val fakeRequest = new FakeRequest[AnyContent](
        Helpers.PUT,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken), "Content-Type" -> Seq("application/json"))),
        AnyContentAsJson(TestData.offerDraftJson),
        secure = true)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "updateOffers must return 200 when offerService returns offer" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = TestData.offerDraft
      val offer = TestData.offer
      offerService.updateOffer(OfferId("1"), Version(1L), offerDraft) returns Future.successful(Option(offer))

      val fakeRequest = new FakeRequest[AnyContent](
        Helpers.PUT,
        "/offer/1/1",
        FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken), "Content-Type" -> Seq("application/json"))),
        AnyContentAsJson(TestData.offerDraftJson),
        secure = true)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }
  }
}

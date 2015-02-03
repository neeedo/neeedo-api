package controllers

import common.domain._
import model.{OfferId, Offer}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Result
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers}
import services.OfferService
import test.TestApplications

import scala.concurrent.Future

class OffersSpec extends Specification with Mockito {
  "Offers Controller" should {

    "createOffer must return 400 missing body for post requests without body" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val res: Future[Result] = ctrl.createOffer()(FakeRequest(Helpers.POST, "/"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }


    "createOffer must return 400 cannot parse json for post requests with invalid offerdraft" in {
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
      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(offerDraftJson)
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "createOffer must return 400 unknown error when offerService returns empty option" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = OfferDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(25.0))
      offerService.createOffer(offerDraft) returns Future.successful(Option.empty)
      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createOffer must return 200 when offerService returns offer" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = OfferDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(25.0))
      val offer = Offer(OfferId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(25.0))
      offerService.createOffer(offerDraft) returns Future.successful(Option(offer))

      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.createOffer()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }

    "getOffer must return 200 and the offer json for a valid id" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offer = Offer(OfferId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(77.0))
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
      val offer = Offer(OfferId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(77.0))
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.successful(Option(offer))

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(FakeRequest(Helpers.DELETE, "/offer/1/1"))

      Helpers.status(res) must equalTo(200)
    }

    "deleteOffer must return 404 for an invalid id or version" in TestApplications.loggingOffApp() {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      offerService.deleteOffer(OfferId("1"), Version(1L)) returns Future.successful(Option.empty[Offer])

      val res: Future[Result] = ctrl.deleteOffer(OfferId("1"), Version(1L))(FakeRequest(Helpers.DELETE, "/offer/1/1"))

      Helpers.status(res) must equalTo(404)
    }

    "deleteOffer must return 400 missing body for put requests without body" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(FakeRequest(Helpers.PUT, "/offer/1/1"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "updateOffers must return 400 cannot parse json for put requests with invalid offer draft" in {
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
      val fakeRequest = FakeRequest(Helpers.PUT, "/offers/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(offerDraftJson)
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "updateOffers must return 400 unknown error when offerService returns empty option" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = OfferDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(25.0))
      offerService.updateOffer(OfferId("1"), Version(1L), offerDraft) returns Future.successful(Option.empty)
      val fakeRequest = FakeRequest(Helpers.PUT, "/demands/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "updateOffers must return 200 when offerService returns offer" in {
      val offerService = mock[OfferService]
      val ctrl = new Offers(offerService)
      val offerDraft = OfferDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(25.0))
      val offer = Offer(OfferId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Price(77.0))
      offerService.updateOffer(OfferId("1"), Version(1L), offerDraft) returns Future.successful(Option(offer))

      val fakeRequest = FakeRequest(Helpers.PUT, "/demands/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(offerDraft))
      val res: Future[Result] = ctrl.updateOffer(OfferId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("offer" -> Json.toJson(offer)).toString())
    }
  }
}

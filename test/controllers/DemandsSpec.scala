package controllers


import common.domain._
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import services.DemandService
import play.api.test.Helpers.defaultAwaitTimeout
import test.TestApplications

import scala.concurrent.Future

class DemandsSpec extends Specification with Mockito {
  "Demands Controller" should {

    "createDemand must return 400 missing body for post requests without body" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val res: Future[Result] = ctrl.createDemand()(FakeRequest(Helpers.POST, "/"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }


    "createDemand must return 400 cannot parse json for post requests with invalid demanddraft" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraftJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212},"distance":30,"price":{"min":25.0,"max":77.0}}""")
      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "createDemand must return 400 unknown error when dermandService returns empty option" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraft = DemandDraft(UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.createDemand(demandDraft) returns Future.successful(Option.empty)
      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(demandDraft))
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createDemand must return 200 when dermandService returns demand" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraft = DemandDraft(UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      val demand = Demand(DemandId("1"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.createDemand(demandDraft) returns Future.successful(Option(demand))

      val fakeRequest = FakeRequest(Helpers.POST, "/")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(demandDraft))
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }

    "getDemand must return 200 and the demand json for a valid id" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demand = Demand(DemandId("1"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.getDemandById(DemandId("1")) returns Future.successful(Option(demand))

      val res: Future[Result] = ctrl.getDemand(DemandId("1"))(FakeRequest())

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }

    "getDemand must return 404 and error json for a invalid id" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      demandService.getDemandById(DemandId("1")) returns Future.successful(Option.empty[Demand])

      val res: Future[Result] = ctrl.getDemand(DemandId("1"))(FakeRequest())

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Demand Entity not found").toString())
    }

    "deleteDemand must return 200 for a valid id and version" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demand = Demand(DemandId("1"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.deleteDemand(DemandId("1"), Version(1L)) returns Future.successful(Option(demand))

      val res: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(FakeRequest(Helpers.DELETE, "/demands/1/1"))

      Helpers.status(res) must equalTo(200)
    }

    "deleteDemand must return 404 for an invalid id or version" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      demandService.deleteDemand(DemandId("1"), Version(1L)) returns Future.successful(Option.empty[Demand])

      val res: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(FakeRequest(Helpers.DELETE, "/demands/1/1"))

      Helpers.status(res) must equalTo(404)
    }

    "updateDemand must return 400 missing body for put requests without body" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val res: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(FakeRequest(Helpers.PUT, "/demands/1/1"))

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body\"}")
    }

    "updateDemand must return 400 cannot parse json for put requests with invalid demand draft" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraftJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212},"distance":30,"price":{"min":25.0,"max":77.0}}""")
      val fakeRequest = FakeRequest(Helpers.PUT, "/demands/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Cannot parse json\"}")
    }

    "updateDemands must return 400 unknown error when dermandService returns empty option" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraft = DemandDraft(UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.updateDemand(DemandId("1"), Version(1L), demandDraft) returns Future.successful(Option.empty)
      val fakeRequest = FakeRequest(Helpers.PUT, "/demands/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(demandDraft))
      val res: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createDemands must return 200 when dermandService returns demand" in {
      val demandService = mock[DemandService]
      val ctrl = new Demands(demandService)
      val demandDraft = DemandDraft(UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      val demand = Demand(DemandId("1"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"), Set("socken", "bekleidung", "wolle"), Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      demandService.updateDemand(DemandId("1"), Version(1L), demandDraft) returns Future.successful(Option(demand))

      val fakeRequest = FakeRequest(Helpers.PUT, "/demands/1/1")
        .withHeaders(("Content-Type","application/json"))
        .withJsonBody(Json.toJson(demandDraft))
      val res: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }
  }
}

package controllers


import common.domain.Version
import common.exceptions.{ElasticSearchIndexFailed, ProductNotFound}
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContent, Result}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import services.{DemandService}
import play.api.test.Helpers.defaultAwaitTimeout
import test.{TestData, TestApplications}

import scala.concurrent.Future

class DemandsControllerSpec extends Specification with Mockito {

  val demandId = TestData.demandId
  val demandVersion = TestData.version
  val demand = TestData.demand
  val demandJson = TestData.demandJson
  val demandDraft = TestData.demandDraft
  val demandDraftJson = TestData.demandDraftJson

  val emptyBodyRequestWithWrongCredentials = new FakeRequest[AnyContent](
    Helpers.POST,
    "/demand",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq("Bla"))),
    AnyContentAsEmpty,
    secure = true)

  val emptyBodyRequestWithoutSsl = new FakeRequest[AnyContent](
    Helpers.POST,
    "/demand",
    FakeHeaders(),
    AnyContentAsEmpty,
    secure = false)

  val emptyBodyCreateFakeRequest = new FakeRequest[AnyContent](
    Helpers.POST,
    "/demand",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
    AnyContentAsEmpty,
    secure = true)

  val emptyBodyDeleteFakeRequest = new FakeRequest[AnyContent](
    Helpers.DELETE,
    "/demand/1/1",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
    AnyContentAsEmpty,
    secure = true)

  val emptyBodyUpdateFakeRequest = new FakeRequest[AnyContent](
    Helpers.PUT,
    "/demand/1/1",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken), "Content-Type" -> Seq("application/json"))),
    AnyContentAsEmpty,
    secure = true)

  val emptyBodyGetFakeRequest = new FakeRequest[AnyContent](
    Helpers.GET,
    "offer/1",
    FakeHeaders(Seq(Helpers.AUTHORIZATION -> Seq(TestData.basicAuthToken))),
    AnyContentAsEmpty,
    secure = true)

  "Demands Controller" should {

    "demand controller must return 401 for wrong user credentials in secured actions" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)

      val create: Future[Result] = ctrl.createDemand()(emptyBodyRequestWithWrongCredentials)
      val delete: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)
      val update: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)

      Helpers.status(create) must equalTo(401)
      Helpers.status(delete) must equalTo(401)
      Helpers.status(update) must equalTo(401)
    }

    "demand controller must return 301 for non https request in secured actions" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)

      val create: Future[Result] = ctrl.createDemand()(emptyBodyRequestWithoutSsl)
      val delete: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithoutSsl)
      val update: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithoutSsl)

      Helpers.status(create) must equalTo(301)
      Helpers.status(delete) must equalTo(301)
      Helpers.status(update) must equalTo(301)
    }

    "createDemand must return 400 missing body for post requests without body" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      val res: Future[Result] = ctrl.createDemand()(emptyBodyCreateFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }


    "createDemand must return 400 cannot parse json for post requests with invalid demanddraft" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      val demandDraftJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212},"distance":30,"price":{"min":25.0,"max":77.0}}""")
      val fakeRequest = emptyBodyCreateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }

    "createDemand must return InternalServerError when dermandService returns empty option" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.createDemand(demandDraft) returns Future.failed(new Exception("Unknown error"))
      val fakeRequest = emptyBodyCreateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createDemand must return 200 when dermandService returns demand" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.createDemand(demandDraft) returns Future(demand)

      val fakeRequest = emptyBodyCreateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> demandJson).toString())
    }

    "getDemand must return 200 and the demand json for a valid id" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.getDemandById(demandId) returns Future.successful(Option(demand))

      val res: Future[Result] = ctrl.getDemandById(demandId)(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> demandJson).toString())
    }

    "getDemand must return 404 and error json for a invalid id" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.getDemandById(demandId) returns Future.successful(Option.empty[Demand])

      val res: Future[Result] = ctrl.getDemandById(demandId)(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Demand not found").toString())
    }

    "deleteDemand must return 200 for a valid id and version" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.deleteDemand(demandId, demandVersion) returns Future(demand)

      val res: Future[Result] = ctrl.deleteDemand(demandId, demandVersion)(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "deleteDemand must return 404 for an invalid id or version" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.deleteDemand(demandId, demandVersion) returns Future.failed(new ProductNotFound(""))

      val res: Future[Result] = ctrl.deleteDemand(demandId, demandVersion)(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

    "updateDemand must return 400 missing body for put requests without body" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      val res: Future[Result] = ctrl.updateDemand(demandId, demandVersion)(emptyBodyCreateFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }


    "updateDemands must return 400 cannot parse json for put requests with invalid demand draft" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      val demandDraftJson = Json.parse("""{"userId":"1","tags":"socken bekleidung wolle","location":{"lat":13.534212},"distance":30,"price":{"min":25.0,"max":77.0}}""")
      val fakeRequest = emptyBodyUpdateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demandId, demandVersion)(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }

    "updateDemands must return 500 internal server error when dermandService returns elasticsearchIndexException" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.updateDemand(demandId, demandVersion, demandDraft) returns Future.failed(new ElasticSearchIndexFailed("Index Failed"))
      val fakeRequest = emptyBodyUpdateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demandId, demandVersion)(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Index Failed\"}")
    }

    "updateDemands must return 200 when dermandService returns demand" in TestApplications.loggingOffApp() {
      val demandService = mock[DemandService]
      val ctrl = new DemandsController(demandService)
      demandService.updateDemand(demandId, demandVersion, demandDraft) returns Future(demand)

      val fakeRequest = emptyBodyUpdateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demandId, demandVersion)(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> demandJson).toString())
    }
  }
}

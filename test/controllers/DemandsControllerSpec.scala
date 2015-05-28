package controllers


import common.domain._
import common.exceptions.{ElasticSearchIndexFailed, ProductNotFound}
import common.helper.SecuredAction
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContent, Result}
import play.api.test.{WithApplication, FakeHeaders, FakeRequest, Helpers}
import services.{UserService, DemandService}
import play.api.test.Helpers.defaultAwaitTimeout
import test.{TestData, TestApplications}

import scala.concurrent.Future

class DemandsControllerSpec extends Specification with Mockito {

  "Demands Controller" should {

    "demand controller must return 401 for wrong user credentials in secured actions" in new DemandsControllerContext {
      val create: Future[Result] = ctrl.createDemand()(emptyBodyRequestWithWrongCredentials)
      val delete: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)
      val update: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithWrongCredentials)

      Helpers.status(create) must equalTo(401)
      Helpers.status(delete) must equalTo(401)
      Helpers.status(update) must equalTo(401)
    }

    "demand controller must return 301 for non https request in secured actions" in new DemandsControllerContext {
      val create: Future[Result] = ctrl.createDemand()(emptyBodyRequestWithoutSsl)
      val delete: Future[Result] = ctrl.deleteDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithoutSsl)
      val update: Future[Result] = ctrl.updateDemand(DemandId("1"), Version(1L))(emptyBodyRequestWithoutSsl)

      Helpers.status(create) must equalTo(301)
      Helpers.status(delete) must equalTo(301)
      Helpers.status(update) must equalTo(301)
    }

    "createDemand must return 400 missing body for post requests without body" in new DemandsControllerContext {
      val res: Future[Result] = ctrl.createDemand()(emptyBodyCreateFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }


    "createDemand must return 400 cannot parse json for post requests with invalid demanddraft" in new DemandsControllerContext {
      val fakeRequest = emptyBodyCreateFakeRequest.withJsonBody(wrongDemandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }

    "createDemand must return InternalServerError when dermandService returns empty option" in new DemandsControllerContext {
      demandService.createDemand(any[DemandDraft]) returns Future.failed(new Exception("Unknown error"))
      val fakeRequest = emptyBodyCreateFakeRequest.withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Unknown error\"}")
    }

    "createDemand must return 200 when dermandService returns demand" in new DemandsControllerContext {
      demandService.createDemand(any[DemandDraft]) returns Future(demand)

      val fakeRequest = emptyBodyCreateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.createDemand()(fakeRequest)

      Helpers.status(res) must equalTo(201)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }

    "getDemand must return 200 and the demand json for a valid id" in new DemandsControllerContext {
      demandService.getDemandById(any[DemandId]) returns Future.successful(Option(demand))

      val res: Future[Result] = ctrl.getDemandById(demand.id)(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }

    "getDemand must return 404 and error json for a invalid id" in new DemandsControllerContext {
      demandService.getDemandById(any[DemandId]) returns Future.successful(Option.empty[Demand])

      val res: Future[Result] = ctrl.getDemandById(demand.id)(emptyBodyGetFakeRequest)

      Helpers.status(res) must equalTo(404)
      Helpers.contentAsString(res) must equalTo(Json.obj("error" -> "Demand not found").toString())
    }

    "deleteDemand must return 200 for a valid id and version" in new DemandsControllerContext {
      demandService.deleteDemand(any[DemandId], any[Version]) returns Future(demand)

      val res: Future[Result] = ctrl.deleteDemand(demand.id, demand.version)(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(200)
    }

    "deleteDemand must return 404 for an invalid id or version" in new DemandsControllerContext {
      demandService.deleteDemand(any[DemandId], any[Version]) returns Future.failed(new ProductNotFound(""))

      val res: Future[Result] = ctrl.deleteDemand(demand.id, demand.version)(emptyBodyDeleteFakeRequest)

      Helpers.status(res) must equalTo(404)
    }

    "updateDemand must return 400 missing body for put requests without body" in new DemandsControllerContext {
      val res: Future[Result] = ctrl.updateDemand(demand.id, demand.version)(emptyBodyCreateFakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Missing body json object\"}")
    }


    "updateDemands must return 400 cannot parse json for put requests with invalid demand draft" in new DemandsControllerContext {
      val fakeRequest = emptyBodyUpdateFakeRequest.withJsonBody(wrongDemandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demand.id, demand.version)(fakeRequest)

      Helpers.status(res) must equalTo(400)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Invalid json body\"}")
    }

    "updateDemands must return 500 internal server error when dermandService returns elasticsearchIndexException" in new DemandsControllerContext {
      demandService.updateDemand(any[DemandId], any[Version], any[DemandDraft]) returns
        Future.failed(new ElasticSearchIndexFailed("Index Failed"))
      val fakeRequest = emptyBodyUpdateFakeRequest.withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demand.id, demand.version)(fakeRequest)

      Helpers.status(res) must equalTo(500)
      Helpers.contentAsString(res) must equalTo("{\"error\":\"Index Failed\"}")
    }

    "updateDemands must return 200 when dermandService returns demand" in new DemandsControllerContext {
      demandService.updateDemand(any[DemandId], any[Version], any[DemandDraft]) returns Future(demand)

      val fakeRequest = emptyBodyUpdateFakeRequest
        .withJsonBody(demandDraftJson)
      val res: Future[Result] = ctrl.updateDemand(demand.id, demand.version)(fakeRequest)

      Helpers.status(res) must equalTo(200)
      Helpers.contentAsString(res) must equalTo(Json.obj("demand" -> Json.toJson(demand)).toString())
    }
  }

  trait DemandsControllerContext extends WithApplication {
    val demandService = mock[DemandService]
    val userService = mock[UserService]
    userService.authorizeUser(any[UserCredentials]) returns Future(Some(UserId("abc")))
    val securedAction = new SecuredAction(userService)
    val ctrl = new DemandsController(demandService, securedAction)

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

    val demand = Demand(
      DemandId("123"),
      Version(1),
      UserId("abc"),
      Set("Socken", "Bekleidung"),
      Set("Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Distance(100),
      Price(0.00),
      Price(10.00)
    )

    val demandDraftJson: JsObject = Json.obj(
      "userId" -> demand.uid.value,
      "mustTags" -> demand.mustTags,
      "shouldTags" -> demand.shouldTags,
      "location" -> Json.obj(
        "lat" -> demand.location.lat.value,
        "lon" -> demand.location.lon.value
      ),
      "distance" -> demand.distance.value,
      "price" -> Json.obj(
        "min" -> demand.priceMin.value,
        "max" -> demand.priceMax.value
      )
    )

    val wrongDemandDraftJson: JsObject = Json.obj(
      "userId" -> demand.uid.value,
      "mustTags" -> demand.mustTags,
      "location" -> Json.obj(
        "lon" -> demand.location.lon.value
      ),
      "distance" -> demand.distance.value,
      "price" -> Json.obj(
        "max" -> demand.priceMax.value
      )
    )
  }
}

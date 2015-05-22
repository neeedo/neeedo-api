package services

import common.domain._
import common.exceptions.{ElasticSearchIndexFailed, SphereIndexFailed}
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import services.es.EsDemandService
import services.sphere.SphereDemandService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class DemandServiceSpec extends Specification with Mockito {

  "DemandService" should {

    "createDemand must throw SphereIndexFailed when sphereDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was no (esDemandServiceMock).createDemand(demand)
    }

    "createDemand must return EsIndexFailed when esDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns Future(demand)
      esDemandServiceMock.createDemand(any[Demand]) returns Future.failed(new ElasticSearchIndexFailed(""))
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns Future(demand)

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[ElasticSearchIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand)
      there was one (sphereDemandServiceMock).deleteDemand(demand.id, demand.version)
    }

    "createDemand must return demand if elasticsearch and sphere succeed" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(draft) returns Future(demand)
      esDemandServiceMock.createDemand(demand) returns Future(demand)

      Await.result(service.createDemand(draft), Duration.Inf) must beEqualTo(demand)
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand)
    }

    "deleteDemand must throw correct exception when SphereDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns
        Future.failed(new Exception())
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future(demand.id)

      Await.result(service.deleteDemand(demand.id, demand.version), Duration.Inf) must
        throwA[Exception]
      there was one (esDemandServiceMock).deleteDemand(demand.id)
      there was one (sphereDemandServiceMock).deleteDemand(demand.id, demand.version)
    }

    "deleteDemand must throw exception when EsDemandService fails" in new DemandServiceContext {
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future.failed(new Exception())

      Await.result(service.deleteDemand(demand.id, demand.version), Duration.Inf) must
        throwA[Exception]
      there was one (esDemandServiceMock).deleteDemand(demand.id)
      there was no (sphereDemandServiceMock).deleteDemand(demand.id, demand.version)
    }

    "deleteDemand must return demand if es and sphere succeed" in new DemandServiceContext {
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future(demand.id)
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns Future(demand)

      Await.result(service.deleteDemand(demand.id, demand.version), Duration.Inf) must
        beEqualTo(demand)
      there was one (esDemandServiceMock).deleteDemand(demand.id)
      there was one (sphereDemandServiceMock).deleteDemand(demand.id, demand.version)
    }
  }

  trait DemandServiceContext extends Scope {
    val esDemandServiceMock = mock[EsDemandService]
    val sphereDemandServiceMock = mock[SphereDemandService]
    val service = new DemandService(sphereDemandServiceMock, esDemandServiceMock)

    val draft = DemandDraft(
      UserId("abc"),
      Set("Socken", "Bekleidung"),
      Set("Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Distance(100),
      Price(0.00),
      Price(10.00)
    )

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
  }
}


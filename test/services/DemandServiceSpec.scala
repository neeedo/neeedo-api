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
      there was no (esDemandServiceMock).createDemand(demand1)
    }

    "createDemand must return EsIndexFailed when esDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns Future(demand1)
      esDemandServiceMock.createDemand(any[Demand]) returns Future.failed(new ElasticSearchIndexFailed(""))
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns Future(demand1)

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[ElasticSearchIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand1)
      there was one (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
    }

    "createDemand must return demand if elasticsearch and sphere succeed" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns Future(demand1)
      esDemandServiceMock.createDemand(any[Demand]) returns Future(demand1)

      Await.result(service.createDemand(draft), Duration.Inf) must beEqualTo(demand1)
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand1)
    }

    "deleteDemand must throw correct exception when SphereDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns
        Future.failed(new Exception())
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future(demand1.id)

      Await.result(service.deleteDemand(demand1.id, demand1.version), Duration.Inf) must
        throwA[Exception]
      there was one (esDemandServiceMock).deleteDemand(demand1.id)
      there was one (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
    }

    "deleteDemand must throw exception when EsDemandService fails" in new DemandServiceContext {
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future.failed(new Exception())

      Await.result(service.deleteDemand(demand1.id, demand1.version), Duration.Inf) must
        throwA[Exception]
      there was one (esDemandServiceMock).deleteDemand(demand1.id)
      there was no (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
    }

    "deleteDemand must return demand if es and sphere succeed" in new DemandServiceContext {
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future(demand1.id)
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns Future(demand1)

      Await.result(service.deleteDemand(demand1.id, demand1.version), Duration.Inf) must
        beEqualTo(demand1)
      there was one (esDemandServiceMock).deleteDemand(demand1.id)
      there was one (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
    }

    "createDemand must throw SphereIndexFailed when sphereDemandService fails" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was no (esDemandServiceMock).createDemand(demand1)
    }

    "updateDemand must return sphereIndexFailed when sphere cant create new demand" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.updateDemand(demand1.id, demand1.version, draft), Duration.Inf) must
        throwA[SphereIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was no (esDemandServiceMock).createDemand(demand2)
      there was no (esDemandServiceMock).deleteDemand(demand1.id)
      there was no (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
    }

    "updateDemand must return EsIndexFailed when es cant create new demand" in new DemandServiceContext {
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns Future(demand2)
      esDemandServiceMock.createDemand(any[Demand]) returns
        Future.failed(new ElasticSearchIndexFailed(""))

      Await.result(service.updateDemand(demand1.id, demand1.version, draft), Duration.Inf) must
        throwA[ElasticSearchIndexFailed]
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand2)
      there was one (sphereDemandServiceMock).deleteDemand(demand2.id, demand2.version)
      there was no (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
      there was no (esDemandServiceMock).deleteDemand(demand1.id)
    }

    "updateDemand must return new demand if es and sphere succeed" in new DemandServiceContext {
      esDemandServiceMock.deleteDemand(any[DemandId]) returns Future(demand1.id)
      sphereDemandServiceMock.deleteDemand(any[DemandId], any[Version]) returns Future(demand1)
      sphereDemandServiceMock.createDemand(any[DemandDraft]) returns Future(demand2)
      esDemandServiceMock.createDemand(any[Demand]) returns Future(demand2)

      Await.result(service.updateDemand(demand1.id, demand1.version, draft), Duration.Inf) must
        beEqualTo(demand2)
      there was one (esDemandServiceMock).deleteDemand(demand1.id)
      there was one (sphereDemandServiceMock).deleteDemand(demand1.id, demand1.version)
      there was one (sphereDemandServiceMock).createDemand(draft)
      there was one (esDemandServiceMock).createDemand(demand2)
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

    val demand1 = Demand(
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

    val demand2 = Demand(
      DemandId("456"),
      Version(1),
      UserId("hans"),
      Set("Fahrrad", "rot"),
      Set("fixie"),
      Location(Longitude(14.2), Latitude(9.5)),
      Distance(50),
      Price(40.00),
      Price(800.00)
    )
  }
}


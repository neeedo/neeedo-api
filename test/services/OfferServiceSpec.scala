package services

import common.domain._
import common.exceptions.{ElasticSearchIndexFailed, SphereIndexFailed}
import model.{DemandId, Demand, OfferId, Offer}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import services.es.{EsMessageService, EsOfferService}
import services.sphere.SphereOfferService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class OfferServiceSpec extends Specification with Mockito {

  trait OfferServiceContext extends Scope {
    val esOfferServiceMock = mock[EsOfferService]
    val sphereOfferServiceMock = mock[SphereOfferService]
    val esMessageServiceMock = mock[EsMessageService]
    val service = new OfferService(sphereOfferServiceMock, esOfferServiceMock, esMessageServiceMock)

    val draft = OfferDraft(
      UserId("abc"),
      Set("Socken"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      Set.empty
    )

    val offer1 = Offer(
      OfferId("123"),
      Version(1),
      UserIdAndName(
        draft.uid,
        Username("test")
      ),
      draft.tags,
      draft.location,
      draft.price,
      draft.images
    )

    val offer2 = Offer(
      OfferId("456"),
      Version(1),
      UserIdAndName(
        UserId("horst"),
        Username("test")
      ),
      Set("Fahrrad"),
      Location(Longitude(8.2), Latitude(13.5)),
      Price(149.00),
      Set.empty
    )
  }

  "OfferService" should {

    "createOffer must throw SphereIndexFailed when sphereOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was no (esOfferServiceMock).createOffer(offer1)
    }

    "createOffer must return EsIndexFailed when esOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer1)
      esOfferServiceMock.createOffer(any[Offer]) returns Future.failed(new ElasticSearchIndexFailed(""))
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer1)

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[ElasticSearchIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer1)
      there was one (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
    }

    "createOffer must return offer if elasticsearch and sphere succeed" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer1)
      esOfferServiceMock.createOffer(any[Offer]) returns Future(offer1)
      esMessageServiceMock.alertDemandsFor(any[OfferId]) returns Future(List.empty)

      Await.result(service.createOffer(draft), Duration.Inf) must beEqualTo(offer1)
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer1)
      there was one (esMessageServiceMock).alertDemandsFor(offer1.id)
    }

    "deleteOffer must throw correct exception when SphereOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns
        Future.failed(new Exception())
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future(offer1.id)

      Await.result(service.deleteOffer(offer1.id, offer1.version), Duration.Inf) must
        throwA[Exception]
      there was one (esOfferServiceMock).deleteOffer(offer1.id)
      there was one (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
    }

    "deleteOffer must throw exception when EsOfferService fails" in new OfferServiceContext {
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future.failed(new Exception())

      Await.result(service.deleteOffer(offer1.id, offer1.version), Duration.Inf) must
        throwA[Exception]
      there was one (esOfferServiceMock).deleteOffer(offer1.id)
      there was no (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
    }

    "deleteOffer must return offer if es and sphere succeed" in new OfferServiceContext {
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future(offer1.id)
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer1)

      Await.result(service.deleteOffer(offer1.id, offer1.version), Duration.Inf) must
        beEqualTo(offer1)
      there was one (esOfferServiceMock).deleteOffer(offer1.id)
      there was one (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
    }

    "updateOffer must return sphereIndexFailed when sphere cant create new offer" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.updateOffer(offer1.id, offer1.version, draft), Duration.Inf) must
        throwA[SphereIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was no (esOfferServiceMock).createOffer(offer2)
      there was no (esOfferServiceMock).deleteOffer(offer1.id)
      there was no (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
    }

    "updateOffer must return EsIndexFailed when es cant create new offer" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer2)
      esOfferServiceMock.createOffer(any[Offer]) returns
        Future.failed(new ElasticSearchIndexFailed(""))

      Await.result(service.updateOffer(offer1.id, offer1.version, draft), Duration.Inf) must
        throwA[ElasticSearchIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer2)
      there was one (sphereOfferServiceMock).deleteOffer(offer2.id, offer2.version)
      there was no (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
      there was no (esOfferServiceMock).deleteOffer(offer1.id)
    }

    "updateOffer must return new offer if es and sphere succeed" in new OfferServiceContext {
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future(offer1.id)
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer1)
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer2)
      esOfferServiceMock.createOffer(any[Offer]) returns Future(offer2)
      esMessageServiceMock.alertDemandsFor(any[OfferId]) returns Future(List.empty)

      Await.result(service.updateOffer(offer1.id, offer1.version, draft), Duration.Inf) must
        beEqualTo(offer2)
      there was one (esOfferServiceMock).deleteOffer(offer1.id)
      there was one (sphereOfferServiceMock).deleteOffer(offer1.id, offer1.version)
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer2)
      there was one (esMessageServiceMock).alertDemandsFor(offer2.id)
    }
  }
}


package services

import common.domain._
import common.exceptions.{ElasticSearchIndexFailed, SphereIndexFailed}
import model.{OfferId, Offer}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import services.es.EsOfferService
import services.sphere.SphereOfferService

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._


class OfferServiceSpec extends Specification with Mockito {

  "OfferService" should {

    "createOffer must throw SphereIndexFailed when sphereOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns
        Future.failed(new SphereIndexFailed(""))

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was no (esOfferServiceMock).createOffer(offer)
    }

    "createOffer must return EsIndexFailed when esOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer)
      esOfferServiceMock.createOffer(any[Offer]) returns Future.failed(new ElasticSearchIndexFailed(""))
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer)

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[ElasticSearchIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer)
      there was one (sphereOfferServiceMock).deleteOffer(offer.id, offer.version)
    }

    "createOffer must return offer if elasticsearch and sphere succeed" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(draft) returns Future(offer)
      esOfferServiceMock.createOffer(offer) returns Future(offer)

      Await.result(service.createOffer(draft), Duration.Inf) must beEqualTo(offer)
      there was one (sphereOfferServiceMock).createOffer(draft)
      there was one (esOfferServiceMock).createOffer(offer)
    }

    "deleteOffer must throw correct exception when SphereOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns
        Future.failed(new Exception())
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future(offer.id)

      Await.result(service.deleteOffer(offer.id, offer.version), Duration.Inf) must
        throwA[Exception]
      there was one (esOfferServiceMock).deleteOffer(offer.id)
      there was one (sphereOfferServiceMock).deleteOffer(offer.id, offer.version)
    }

    "deleteOffer must throw exception when EsOfferService fails" in new OfferServiceContext {
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future.failed(new Exception())

      Await.result(service.deleteOffer(offer.id, offer.version), Duration.Inf) must
        throwA[Exception]
      there was one (esOfferServiceMock).deleteOffer(offer.id)
      there was no (sphereOfferServiceMock).deleteOffer(offer.id, offer.version)
    }

    "deleteOffer must return offer if es and sphere succeed" in new OfferServiceContext {
      esOfferServiceMock.deleteOffer(any[OfferId]) returns Future(offer.id)
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer)

      Await.result(service.deleteOffer(offer.id, offer.version), Duration.Inf) must
        beEqualTo(offer)
      there was one (esOfferServiceMock).deleteOffer(offer.id)
      there was one (sphereOfferServiceMock).deleteOffer(offer.id, offer.version)
    }
  }

  trait OfferServiceContext extends Scope {
    val esOfferServiceMock = mock[EsOfferService]
    val sphereOfferServiceMock = mock[SphereOfferService]
    val service = new OfferService(sphereOfferServiceMock, esOfferServiceMock)

    val draft = OfferDraft(
      UserId("abc"),
      Set("Socken"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      Set.empty
    )

    val offer = Offer(
      OfferId("123"),
      Version(1),
      UserId("abc"),
      Set("Socken"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      Set.empty
    )
  }
}


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
      there was one (sphereOfferServiceMock).createOffer(any[OfferDraft])
      there was no (esOfferServiceMock).createOffer(any[Offer])
    }

    "createOffer must return EsIndexFailed when sphereOfferService fails" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer)
      sphereOfferServiceMock.deleteOffer(any[OfferId], any[Version]) returns Future(offer)
      esOfferServiceMock.createOffer(any[Offer]) returns
        Future.failed(new ElasticSearchIndexFailed(""))

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[ElasticSearchIndexFailed]
      there was one (sphereOfferServiceMock).createOffer(any[OfferDraft])
      there was one (esOfferServiceMock).createOffer(any[Offer])
      there was one (sphereOfferServiceMock).deleteOffer(any[OfferId], any[Version])
    }

    "createOffer must return offer if es and sphere are succeeding" in new OfferServiceContext {
      sphereOfferServiceMock.createOffer(any[OfferDraft]) returns Future(offer)
      esOfferServiceMock.createOffer(any[Offer]) returns Future(offer)

      Await.result(service.createOffer(draft), Duration.Inf) must beEqualTo(offer)
      there was one (sphereOfferServiceMock).createOffer(any[OfferDraft])
      there was one (esOfferServiceMock).createOffer(any[Offer])
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
      Price(50.00)
    )

    val offer = Offer(
      OfferId("123"),
      Version(1),
      UserId("abc"),
      Set("Socken"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      List()
    )
  }
}


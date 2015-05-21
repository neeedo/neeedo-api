package services.es

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ProductNotFound, ElasticSearchDeleteFailed, ElasticSearchIndexFailed}
import common.helper.ConfigLoader
import model.{Offer, OfferId}
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.test.WithApplication
import services.EsCompletionService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class EsOfferServiceSpec extends Specification with Mockito {

  "EsOfferService" should {
    "createOffer must throw IndexFailedException when IndexResponse is negative" in new EsOfferServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future(negativeIndexResponse)

      Await.result(service.createOffer(offer), Duration.Inf) must
        throwA(new ElasticSearchIndexFailed("Error while saving offer in elasticsearch"))
      there was one (esClientMock).indexDocument(offer.id.value, indexName, typeName, service.buildEsOfferJson(offer))
    }

    "createOffer must throw IndexFailedException when elasticsearch throws an exception" in new EsOfferServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future.failed(new IllegalArgumentException())

      Await.result(service.createOffer(offer), Duration.Inf) must
        throwA(new ElasticSearchIndexFailed("Error while saving offer in elasticsearch"))
      there was one (esClientMock).indexDocument(offer.id.value, indexName, typeName, service.buildEsOfferJson(offer))
    }

    "createOffer must return offer when everything succeeeds" in new EsOfferServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future(positiveIndexResponse)

      Await.result(service.createOffer(offer), Duration.Inf) must beEqualTo(offer)
      there was one (esClientMock)
        .indexDocument(offer.id.value, indexName, typeName, service.buildEsOfferJson(offer))
    }

    "parseIndexResponse must throw exception for negative indexResponse" in new EsOfferServiceContext {
      service.parseIndexResponse(negativeIndexResponse, offer) must
        throwA(new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative"))
    }

    "parseIndexResponse must return offer for positive indexResponse" in new EsOfferServiceContext {
      service.parseIndexResponse(positiveIndexResponse, offer) must beEqualTo(offer)
    }

    "deleteOffer must throw EsDeleteFailed when elasticsearch throws an exception" in new EsOfferServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns
        Future.failed(new Exception())

      Await.result(service.deleteOffer(offer.id), Duration.Inf) must throwA[ElasticSearchDeleteFailed]
      there was one (esClientMock)
        .deleteDocument(offer.id.value, indexName, typeName)
    }

    "deleteOffer must throw ProductNotFound when elasticsearch returns not found" in new EsOfferServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns Future(false)

      Await.result(service.deleteOffer(offer.id), Duration.Inf) must throwA[ProductNotFound]
      there was one (esClientMock)
        .deleteDocument(offer.id.value, indexName, typeName)
    }

    "deleteOffer must return offerId when elasticsearch returns deleted" in new EsOfferServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns Future(true)

      Await.result(service.deleteOffer(offer.id), Duration.Inf) must beEqualTo(offer.id)
      there was one (esClientMock)
        .deleteDocument(offer.id.value, indexName, typeName)
    }
  }

  trait EsOfferServiceContext extends WithApplication {

    val config = Map("offer.typeName" -> "offer")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val indexName = configLoader.offerIndex
    val typeName = indexName.toTypeName
    val esClientMock = mock[ElasticsearchClient]
    val esCompletionServiceMock = mock[EsCompletionService]
    val service = new EsOfferService(esClientMock, configLoader, esCompletionServiceMock)

    val negativeIndexResponse = new IndexResponse("", "", "", 1L, false)
    val positiveIndexResponse = new IndexResponse("", "", "", 1L, true)

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

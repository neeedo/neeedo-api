package services.es

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ProductNotFound, ElasticSearchDeleteFailed, ElasticSearchIndexFailed}
import common.helper.ConfigLoader
import model.{Demand, DemandId}
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.test.WithApplication
import services.es.EsCompletionService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class EsDemandServiceSpec extends Specification with Mockito {

  trait EsDemandServiceContext extends WithApplication {
    val config = Map("demand.typeName" -> "demand")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val indexName = configLoader.demandIndex
    val typeName = indexName.toTypeName
    val esClientMock = mock[ElasticsearchClient]
    val esCompletionServiceMock = mock[EsCompletionService]
    val service = new EsDemandService(esClientMock, configLoader, esCompletionServiceMock)

    val negativeIndexResponse = new IndexResponse("", "", "", 1L, false)
    val positiveIndexResponse = new IndexResponse("", "", "", 1L, true)

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

  "EsDemandService" should {
    "createDemand must throw IndexFailedException when IndexResponse is negative" in new EsDemandServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future(negativeIndexResponse)

      Await.result(service.createDemand(demand), Duration.Inf) must
        throwA(new ElasticSearchIndexFailed("Error while saving demand in elasticsearch"))
      there was one (esClientMock).indexDocument(demand.id.value, indexName, typeName, service.buildEsDemandJson(demand))
    }

    "createDemand must throw IndexFailedException when elasticsearch throws an exception" in new EsDemandServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future.failed(new IllegalArgumentException())

      Await.result(service.createDemand(demand), Duration.Inf) must
        throwA(new ElasticSearchIndexFailed("Error while saving demand in elasticsearch"))
      there was one (esClientMock).indexDocument(demand.id.value, indexName, typeName, service.buildEsDemandJson(demand))
    }

    "createDemand must return demand when everything succeeeds" in new EsDemandServiceContext {
      esClientMock.indexDocument(anyString, any[IndexName], any[TypeName], any[JsValue]) returns
        Future(positiveIndexResponse)

      Await.result(service.createDemand(demand), Duration.Inf) must beEqualTo(demand)
      there was one (esClientMock)
        .indexDocument(demand.id.value, indexName, typeName, service.buildEsDemandJson(demand))
    }

    "parseIndexResponse must throw exception for negative indexResponse" in new EsDemandServiceContext {
      service.parseIndexResponse(negativeIndexResponse, demand) must
        throwA(new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative"))
    }

    "parseIndexResponse must return demand for positive indexResponse" in new EsDemandServiceContext {
      service.parseIndexResponse(positiveIndexResponse, demand) must beEqualTo(demand)
    }

    "deleteDemand must throw EsDeleteFailed when elasticsearch throws an exception" in new EsDemandServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns
        Future.failed(new Exception())

      Await.result(service.deleteDemand(demand.id), Duration.Inf) must throwA[ElasticSearchDeleteFailed]
      there was one (esClientMock)
        .deleteDocument(demand.id.value, indexName, typeName)
    }

    "deleteDemand must throw ProductNotFound when elasticsearch returns not found" in new EsDemandServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns Future(false)

      Await.result(service.deleteDemand(demand.id), Duration.Inf) must throwA[ProductNotFound]
      there was one (esClientMock)
        .deleteDocument(demand.id.value, indexName, typeName)
    }

    "deleteDemand must return demandId when elasticsearch returns deleted" in new EsDemandServiceContext {
      esClientMock.deleteDocument(anyString, any[IndexName], any[TypeName]) returns Future(true)

      Await.result(service.deleteDemand(demand.id), Duration.Inf) must beEqualTo(demand.id)
      there was one (esClientMock)
        .deleteDocument(demand.id.value, indexName, typeName)
    }
  }
}

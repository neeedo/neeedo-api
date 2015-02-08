package migrations

import java.util.concurrent.TimeUnit

import common.domain.{TypeName, IndexName}
import common.elasticsearch.{EsMapping, ElasticsearchClient, ElasticsearchClientFactory}
import org.elasticsearch.common.settings.ImmutableSettings
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.TestApplications

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

class ProductTypeEsMigrationsSpec extends Specification with Mockito {
  "ProductTypeEsMigrations" should {
    "buildIndexRequest must create correct IndexCreationBuilder" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer", "demand.typeName" -> "demand")) {

      val elasticsearch = ElasticsearchClientFactory.instance
      val esMock = mock[ElasticsearchClient]
      val demandIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("demand").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("demand", "")
      val offerIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("offer").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("offer", "")
      esMock.buildIndexRequest(IndexName("demand"), EsMapping(TypeName("demand"), "migrations/demand-mapping.json")) returns demandIndexRequestBuilder
      esMock.buildIndexRequest(IndexName("offer"), EsMapping(TypeName("offer"), "migrations/offer-mapping.json")) returns offerIndexRequestBuilder
      esMock.createIndex(demandIndexRequestBuilder) returns Future.successful(true)
      esMock.createIndex(offerIndexRequestBuilder) returns Future.successful(true)

      val productTypeEsMigrations = new ProductTypeEsMigrations(esMock)
      Await.result(productTypeEsMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

      there was one (esMock).createIndex(demandIndexRequestBuilder)
      there was one (esMock).createIndex(offerIndexRequestBuilder)
    }
  }
}

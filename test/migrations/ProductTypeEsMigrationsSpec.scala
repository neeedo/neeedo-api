package migrations

import java.util.concurrent.TimeUnit

import common.domain.{IndexName, TypeName}
import common.elasticsearch.{ElasticsearchClient, ElasticsearchClientFactory, EsMapping}
import common.helper.ConfigLoader
import org.elasticsearch.common.settings.ImmutableSettings
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.TestApplications

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

class ProductTypeEsMigrationsSpec extends Specification with Mockito {
  "ProductTypeEsMigrations" should {
    "create indices when indices dont exist and cluster state is green" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer", "demand.typeName" -> "demand")) {

        val config = new ConfigLoader
      val elasticsearch = new ElasticsearchClientFactory(config).instance
      val esMock = mock[ElasticsearchClient]
      val demandIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("demand").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("demand", "")
      val offerIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("offer").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("offer", "")
      esMock.buildIndexRequest(IndexName("demand"), EsMapping(TypeName("demand"), "migrations/demand-mapping.json")) returns demandIndexRequestBuilder
      esMock.buildIndexRequest(IndexName("offer"), EsMapping(TypeName("offer"), "migrations/offer-mapping.json")) returns offerIndexRequestBuilder
      esMock.createIndex(IndexName("demand"), demandIndexRequestBuilder) returns Future.successful(true)
      esMock.createIndex(IndexName("offer"), offerIndexRequestBuilder) returns Future.successful(true)
      esMock.waitForGreenStatus returns Future.successful(true)

      val productTypeEsMigrations = new ProductTypeEsMigrations(esMock, config)
      Await.result(productTypeEsMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

      there was one (esMock).createIndex(IndexName("demand"), demandIndexRequestBuilder)
      there was one (esMock).createIndex(IndexName("offer"), offerIndexRequestBuilder)
    }

    "create no indices when indices cluster state is not green" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer", "demand.typeName" -> "demand")) {

        val config = new ConfigLoader
        val elasticsearch = new ElasticsearchClientFactory(config).instance
        val esMock = mock[ElasticsearchClient]
        val demandIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("demand").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("demand", "")
        val offerIndexRequestBuilder = elasticsearch.client.admin().indices().prepareCreate("offer").setSettings(ImmutableSettings.settingsBuilder().put("number_of_shards", 5).put("number_of_replicas", 0).build()).addMapping("offer", "")
        esMock.buildIndexRequest(IndexName("demand"), EsMapping(TypeName("demand"), "migrations/demand-mapping.json")) returns demandIndexRequestBuilder
        esMock.buildIndexRequest(IndexName("offer"), EsMapping(TypeName("offer"), "migrations/offer-mapping.json")) returns offerIndexRequestBuilder
        esMock.waitForGreenStatus returns Future.successful(false)

        val productTypeEsMigrations = new ProductTypeEsMigrations(esMock, config)
        Await.result(productTypeEsMigrations.run(), new FiniteDuration(10, TimeUnit.SECONDS))

        there was no (esMock).createIndex(IndexName("demand"), demandIndexRequestBuilder)
        there was no (esMock).createIndex(IndexName("offer"), offerIndexRequestBuilder)
      }
  }
}

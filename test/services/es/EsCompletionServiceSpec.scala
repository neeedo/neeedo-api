package services.es

import common.domain.{CompletionTagResult, CompletionTag}
import common.elasticsearch.{EsMapping, TestEsClient, ElasticsearchClient}
import common.exceptions.ElasticSearchQueryFailed
import common.helper.ConfigLoader
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.common.xcontent.{ToXContent, XContentBuilder, XContentFactory}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.WithApplication
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class EsCompletionServiceSpec extends Specification with Mockito {
  trait EsCompletionServiceContext extends WithApplication {
    val esMock = mock[ElasticsearchClient]
    val config = Map("completionsIndexName" -> "completions")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val service = new EsCompletionService(esMock, configLoader)
    val completionTag = CompletionTag("Meri")
  }

  trait EsCompletionServiceIntegrationContext extends WithApplication with EsCompletionServiceContext {
    val esClient = new TestEsClient()
    val integrationService = new EsCompletionService(esClient, configLoader)
  }

  "EsCompletionService" should {
    "buildSuggestions must return correct SuggestBuilder" in new EsCompletionServiceContext {
      val builder: XContentBuilder = XContentFactory.jsonBuilder.prettyPrint()
      service.buildSuggestions(completionTag).toXContent(builder, ToXContent.EMPTY_PARAMS)

      builder.string must be equalTo "\n\"tagCompletion\"{\n  \"text\" : \"Meri\",\n  \"completion\" : {\n    \"field\" : \"tag\",\n    \"fuzzy\" : {\n      \"fuzziness\" : \"AUTO\"\n    }\n  }\n}"
    }

    "buildTagCompletionJson must return correct Json" in new EsCompletionServiceContext {
      service.buildTagCompletionJson(completionTag) must be equalTo
      Json.obj("tag" -> Json.obj("input" -> "Meri", "output" -> "Meri", "weight" -> 1))
    }

    "service must add and get completion correctly" in new EsCompletionServiceIntegrationContext {
      val indexRequest = esClient.buildIndexRequest(
        configLoader.completionsIndex,
        EsMapping(configLoader.completionsIndex.toTypeName, "migrations/completions-mapping.json"))

      Await.result(esClient.createIndex(configLoader.completionsIndex, indexRequest), Duration.Inf) must be equalTo
        true
      Await.result(integrationService.getCompletions(CompletionTag("Mer")), Duration.Inf) must throwA[ElasticSearchQueryFailed]
      Await.result(integrationService.upsertCompletions(List(CompletionTag("Merida"))), Duration.Inf).map {
        resp => resp.isCreated must be equalTo true
      }
      esClient.client.admin().indices()
        .refresh(new RefreshRequest(configLoader.completionsIndex.value)).actionGet()
      Await.result(integrationService.getCompletions(CompletionTag("Mer")), Duration.Inf) must be equalTo
        CompletionTagResult(List("Merida"))
    }
  }
}

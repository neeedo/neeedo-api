package services.es

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchQueryFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions._
import org.elasticsearch.action.suggest.SuggestResponse
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.common.unit.Fuzziness
import org.elasticsearch.script.ScriptService.ScriptType
import org.elasticsearch.search.suggest.SuggestBuilders
import org.elasticsearch.search.suggest.completion.CompletionSuggestion
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConverters._
import scala.concurrent.Future

class EsCompletionService(elasticsearchClient: ElasticsearchClient, config: ConfigLoader) {
  val suggestionName: String = "tagCompletion"

  private[es] def buildSuggestions(tag: CompletionTag) =
    SuggestBuilders
      .fuzzyCompletionSuggestion(suggestionName)
      .field("tag")
      .text(tag.value)
      .setFuzziness(Fuzziness.AUTO)

  def getCompletions(tag: CompletionTag): Future[CompletionTagResult] = {
    val response: Future[SuggestResponse] = elasticsearchClient.client
      .prepareSuggest(config.completionsIndex.value)
      .addSuggestion(buildSuggestions(tag))
      .execute()
      .asScala

    response
      .map(res => CompletionTagResult(getCompletionsFromSuggestResponse(res)))
      .recover {
        case e: Exception =>
          throw new ElasticSearchQueryFailed("Failed to get receive tag completions from Elasticsearch")
      }
  }

  private[es] def buildTagCompletionJson(tag: CompletionTag) = {
    Json.obj(
      "tag" -> Json.obj(
        "input" -> tag.value,
        "output" -> tag.value,
        "weight" -> 1
      )
    )
  }

  def upsertCompletions(tags: List[CompletionTag]): Future[List[UpdateResponse]] = {
    Future.sequence {
      tags.map {
        tag =>
          elasticsearchClient.client
            .prepareUpdate(config.completionsIndex.value, config.completionsIndex.value, tag.value)
            .setScript("tagCompletionUpsert", ScriptType.FILE)
            .setUpsert(buildTagCompletionJson(tag).toString())
            .execute()
            .asScala
      }
    }
  }

  private[es] def getCompletionsFromSuggestResponse(resp: SuggestResponse): List[String] = {
    val completion: CompletionSuggestion = resp.getSuggest.getSuggestion(suggestionName)
    val entries = completion.getEntries.get(0).getOptions.iterator().asScala.toList
    entries.map(_.getText.string())
  }
}

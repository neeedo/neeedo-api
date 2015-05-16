package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchQueryFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.suggest.SuggestResponse
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.script.ScriptService.ScriptType
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.ChiSquare.ChiSquareBuilder
import org.elasticsearch.search.suggest.SuggestBuilders
import org.elasticsearch.search.suggest.completion.CompletionSuggestion
import play.api.libs.json.Json

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


class CompletionService(esCompletionService: EsCompletionService) {

  def completeTag(tag: CompletionTag): Future[CompletionTagResult] = {
    esCompletionService.getCompletions(tag)
  }

  def suggestTags(offerOrDemand: String, phrase: CompletionPhrase): Future[CompletionPhraseResult] = {
      esCompletionService.getSuggestions(offerOrDemand, phrase)
  }

  def writeCompletions(tags: List[CompletionTag]) = {
    esCompletionService.upsertCompletions(tags)
  }
}

class EsCompletionService(elasticsearchClient: ElasticsearchClient, config: ConfigLoader) {
  val aggregationName: String = "tags"
  val suggestionName: String = "tagCompletion"

  def buildSuggestions(tag: CompletionTag) =
    SuggestBuilders
      .completionSuggestion(suggestionName)
      .field("tag")
      .text(tag.value)

  def getCompletions(tag: CompletionTag): Future[CompletionTagResult] = {
    val response: Future[SuggestResponse] = elasticsearchClient.client
      .prepareSuggest(config.completionsIndex.value)
      .addSuggestion(buildSuggestions(tag))
      .execute()
      .asScala

    getCompletionsFromSuggestResponse(response) map(CompletionTagResult(_))
  }

  def upsertCompletions(tags: List[CompletionTag]): Future[List[UpdateResponse]] = {
    def buildTagCompletionJson(tag: CompletionTag) = {
      Json.obj(
        "tag" -> Json.obj(
          "input" -> tag.value,
          "output" -> tag.value,
          "weight" -> 1
        )
      )
    }

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

  def getCompletionsFromSuggestResponse(resp: Future[SuggestResponse]): Future[List[String]] = {
    resp.map {
      res =>
        val completion: CompletionSuggestion = res.getSuggest.getSuggestion(suggestionName)
        val entries = completion.getEntries.get(0).getOptions.iterator().toList
        entries.map(_.getText.string())
    } recover {
      case e: Exception =>
        throw new ElasticSearchQueryFailed("Failed to get receive tag completions from Elasticsearch")
    }
  }

  def buildPhraseCompletionQuery(phrase: CompletionPhrase) =
    QueryBuilders
      .termsQuery("completionTags", phrase.value.asJava)
      .minimumShouldMatch("1")

  def buildAggregation(index: IndexName) = {
    AggregationBuilders
      .significantTerms(aggregationName)
      .field("completionTags")
      .minDocCount(1)
      .significanceHeuristic(new ChiSquareBuilder(false, false))
      .size(20)
  }

  def getSuggestions(index: String, phrase: CompletionPhrase): Future[CompletionPhraseResult] = {
    getEsIndex(index) match {
      case Success(i) =>
        val query = buildPhraseCompletionQuery(phrase)
        val aggregation = buildAggregation(i)

        val searchresponse = elasticsearchClient.client
          .prepareSearch(i.value)
          .setTypes(i.toTypeName.value)
          .setSize(0)
          .setQuery(query)
          .addAggregation(aggregation)
          .execute()
          .asScala

        getBucketsFromSearchresponse(searchresponse, phrase) map(CompletionPhraseResult(_))
      case Failure(e) => Future.failed(e)
    }
  }

  def getBucketsFromSearchresponse(resp: Future[SearchResponse], phrase: CompletionPhrase): Future[List[String]] = {
    resp.map {
      res =>
        res.getAggregations.get[SignificantTerms](aggregationName).asScala.toList
        .map { x: Bucket => x.getKey }
        .filterNot(phrase.value.toSet)
    } recover {
      case e: Exception => throw new ElasticSearchQueryFailed("Failed to get receive tag suggets from Elasticsearch")
    }
  }

  def getEsIndex(offerOrDemand: String): Try[IndexName] = {
    Try {
      offerOrDemand match {
        case "offer" => config.offerIndex
        case "demand" => config.demandIndex
        case _ => throw new IllegalArgumentException("Type must be either demand or offer")
      }
    }
  }
}
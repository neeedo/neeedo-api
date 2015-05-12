package services

import common.domain._
import common.elasticsearch.{EsIndices, ElasticsearchClient}
import common.exceptions.ElasticSearchQueryFailed
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.ChiSquare.ChiSquareBuilder
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.{ChiSquare, SignificanceHeuristic}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Try, Failure}
import scala.collection.JavaConverters._
import common.helper.ImplicitConversions.ActionListenableFutureConverter

class CompletionService(esCompletionService: EsCompletionService) {

  def completeTag(offerOrDemand: String, tag: CompletionTag): Future[CompletionTagResult] = {
      Future(CompletionTagResult(tag.value))
  }

  def suggestTags(offerOrDemand: String, phrase: CompletionPhrase): Future[CompletionPhraseResult] = {
      esCompletionService.getSuggestions(offerOrDemand, phrase)
  }
}

class EsCompletionService(elasticsearchClient: ElasticsearchClient) {

  val aggregationName: String = "tags"

  def buildPhraseCompletionQuery(index: IndexName, phrase: CompletionPhrase) =
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
        val query = buildPhraseCompletionQuery(i, phrase)
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
        case "offer" => EsIndices.offerIndexName
        case "demand" => EsIndices.demandIndexName
        case _ => throw new IllegalArgumentException("Type must be either demand or offer")
      }
    }
  }
}
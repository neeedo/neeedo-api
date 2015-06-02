package services.es

import common.domain.{CompletionPhraseResult, CompletionPhrase}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchQueryFailed
import common.helper.ConfigLoader
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.ChiSquare.ChiSquareBuilder
import scala.collection.JavaConverters._
import scala.concurrent.Future
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import scala.concurrent.ExecutionContext.Implicits.global

class EsSuggestionService(elasticsearchClient: ElasticsearchClient, config: ConfigLoader) {
  val aggregationName: String = "tags"
  val modelFieldName: String = "completionTags"
  private val demandIndex = config.demandIndex
  private val offerIndex = config.offerIndex

  def getSuggestions(phrase: CompletionPhrase): Future[CompletionPhraseResult] = {
    val searchresponse = elasticsearchClient.client
      .prepareSearch(demandIndex.value, offerIndex.value)
      .setTypes(demandIndex.toTypeName.value, offerIndex.toTypeName.value)
      .setSize(0)
      .setQuery(buildPhraseCompletionQuery(phrase))
      .addAggregation(buildAggregation)
      .execute()
      .asScala

    searchresponse map {
      resp =>
        CompletionPhraseResult(getBucketsFromSearchresponse(resp, phrase))
    } recover {
      case e: Exception =>
        throw new ElasticSearchQueryFailed("Failed to get receive tag suggests from Elasticsearch")
    }
  }

  private[es] def buildPhraseCompletionQuery(phrase: CompletionPhrase) = {
    QueryBuilders
      .termsQuery(modelFieldName, phrase.value.asJava)
      .minimumMatch(calcShouldMatch(phrase.value.length))
  }


  private[es] def calcShouldMatch(tagsCount: Int): Int =
    (1.5 * Math.log(tagsCount) + 1).toInt

  private[es] def buildAggregation = {
    AggregationBuilders
      .significantTerms(aggregationName)
      .field(modelFieldName)
      .minDocCount(2)
      .significanceHeuristic(new ChiSquareBuilder(false, false))
      .size(20)
  }

  private[es] def getBucketsFromSearchresponse(res: SearchResponse, phrase: CompletionPhrase): List[String] = {
    res.getAggregations.get[SignificantTerms](aggregationName).asScala.toList
      .map { x: Bucket => x.getKey }
      .filterNot(phrase.value.toSet)
  }
}
package services.es

import common.domain.{CompletionPhrase, CompletionPhraseResult}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchQueryFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.ChiSquare.ChiSquareBuilder
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.GND.GNDBuilder
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.MutualInformation.MutualInformationBuilder

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsSuggestionService(elasticsearchClient: ElasticsearchClient, config: ConfigLoader) {
  val aggregationName: String = "tags"
  val modelFieldName: String = "completionTags"
  private val demandIndex = config.demandIndex
  private val offerIndex = config.offerIndex

  def getSuggestions(phrase: CompletionPhrase): Future[CompletionPhraseResult] = {
    elasticsearchClient.client
      .prepareSearch(demandIndex.value, offerIndex.value)
      .setTypes(demandIndex.toTypeName.value, offerIndex.toTypeName.value)
      .setSize(0)
      .setQuery(buildPhraseCompletionQuery(phrase))
      .addAggregation(buildAggregation(phrase))
      .execute()
      .asScala.map {
        resp =>
          CompletionPhraseResult(getBucketsFromSearchresponse(resp, phrase))
    } recover {
      case e: Exception =>
        throw new ElasticSearchQueryFailed("Failed to get receive tag suggests from Elasticsearch")
    }
  }

  private[es] def buildPhraseCompletionQuery(phrase: CompletionPhrase) = {

    phrase.value.foldLeft(QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())) {
      (acc, elem) => acc.should(QueryBuilders.matchQuery(modelFieldName, elem))
    }.minimumNumberShouldMatch(calcShouldMatch(phrase.value.size))
  }

  private[es] def calcShouldMatch(tagsCount: Int): Int =
    (1.5 * Math.log(tagsCount) + 1).toInt

  private[es] def buildAggregation(phrase: CompletionPhrase) = {
    AggregationBuilders
      .significantTerms(aggregationName)
      .field(modelFieldName)
      .minDocCount(1)
      .significanceHeuristic(new ChiSquareBuilder(false, false))
      .size(5)
      .exclude(phrase.value.map(_.toLowerCase).toArray[String])
  }

  private[es] def getBucketsFromSearchresponse(res: SearchResponse, phrase: CompletionPhrase): List[String] = {
    res.getAggregations.get[SignificantTerms](aggregationName).asScala.toList
      .map { x: Bucket => x.getKey }
      .distinct
  }
}

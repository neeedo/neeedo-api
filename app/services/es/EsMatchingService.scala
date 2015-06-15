package services.es

import common.domain.{EsMatchingResult, Pager}
import common.elasticsearch.ElasticsearchClient
import common.helper.{TimeHelper, ConfigLoader}
import model.{Card, Demand, Offer, OfferId}
import org.elasticsearch.action.search.{SearchRequestBuilder, SearchResponse}
import org.elasticsearch.common.geo.GeoPoint
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.index.query._
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import org.elasticsearch.index.query.functionscore.{FunctionScoreQueryBuilder, ScoreFunctionBuilders}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class EsMatchingService(elasticsearch: ElasticsearchClient, config: ConfigLoader, timeHelper: TimeHelper) {

  def matchDemand(pager: Pager, demand: Demand): Future[List[Offer]] = {
    val query = buildMatchingQuery(demand)

    elasticsearch.client
      .prepareSearch(config.offerIndex.value)
      .setQuery(query)
      .setSize(pager.limit)
      .setFrom(pager.offset)
      .execute()
      .asScala
      .map {
        response => elasticsearch.searchresponseAs[Offer](response)
      }
  }

  def buildMatchingQuery(d: Demand): FunctionScoreQueryBuilder = {
    val baseQuery = QueryBuilders.functionScoreQuery(
      FilterBuilders.andFilter(
          FilterBuilders
            .geoDistanceFilter("location")
            .distance(d.distance.value, DistanceUnit.KILOMETERS)
            .point(d.location.lat.value, d.location.lon.value),
          FilterBuilders
            .termsFilter("tags", d.mustTags.asJava)
      ),
      ScoreFunctionBuilders
        .gaussDecayFunction("createdAt", timeHelper.now, "8h")
        .setDecay(0.75)
        .setOffset("4h")
    ).add(
      ScoreFunctionBuilders
        .gaussDecayFunction("location", new GeoPoint(d.location.lat.value, d.location.lon.value), "10km")
        .setDecay(0.9)
        .setOffset("30km")
    )

    d.shouldTags.foldLeft(baseQuery) {
      case (query, elem) =>
        query.add(
          FilterBuilders.termFilter("shouldTags", elem),
          ScoreFunctionBuilders.weightFactorFunction(1)
        )
    }
  }
}

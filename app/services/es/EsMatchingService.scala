package services.es

import common.domain.Pager
import common.elasticsearch.ElasticsearchClient
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.helper.{ConfigLoader, TimeHelper}
import model.{Demand, Offer}
import org.elasticsearch.common.geo.GeoPoint
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.index.query._
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders

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

  def buildMatchingQuery(d: Demand): QueryBuilder = {
    QueryBuilders.filteredQuery(
      buildMatchingScoreQuery(d),
      FilterBuilders.andFilter(
        FilterBuilders
          .geoDistanceFilter("location")
          .distance(d.distance.value, DistanceUnit.KILOMETERS)
          .point(d.location.lat.value, d.location.lon.value),
        FilterBuilders
          .termsFilter("tags", d.mustTags.map(_.toLowerCase).asJava)
          .execution("and"),
        FilterBuilders
          .rangeFilter("price").from(d.priceMin.value).to(d.priceMax.value)
      )
    )
  }

  private[es] def buildMatchingScoreQuery(d: Demand) = {
    QueryBuilders.functionScoreQuery(
      if (d.shouldTags.isEmpty) QueryBuilders.matchAllQuery()
      else QueryBuilders.termsQuery("tags", d.shouldTags.map(_.toLowerCase).asJava),
      ScoreFunctionBuilders
        .gaussDecayFunction("createdAt", timeHelper.now, "8h")
        .setDecay(0.75)
        .setOffset("4h")
    ).add(
        ScoreFunctionBuilders
          .gaussDecayFunction("location", new GeoPoint(d.location.lat.value, d.location.lon.value), s"${d.distance.value / 2.0}km")
          .setDecay(0.5)
          .setOffset("1km")
    ).scoreMode("avg")
  }
}

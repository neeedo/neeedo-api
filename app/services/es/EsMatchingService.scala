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

  def matchOffer(pager: Pager, offer: Offer): Future[List[Demand]] = {
//    val query = buildMatchOfferQuery(offer)
//
//    elasticsearch.client
//      .prepareSearch(config.demandIndex.value)
//      .setQuery(query)
//      .setSize(pager.limit)
//      .setFrom(pager.offset)
//      .execute()
//      .asScala
//      .map {
//        response => elasticsearch.searchresponseAs[Demand](response)
//      }
    ???
  }

  def matchDemand(pager: Pager, demand: Demand): Future[List[Offer]] = {
    val query = buildMatchDemandQuery(demand)

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

  def buildMatchDemandQuery(d: Demand): QueryBuilder = {
    QueryBuilders.filteredQuery(
      buildMatchingScoreQuery(d),
      FilterBuilders.andFilter(
        FilterBuilders
          .geoDistanceFilter("location")
          .distance(d.distance.value, DistanceUnit.KILOMETERS)
          .point(d.location.lat.value, d.location.lon.value),
        FilterBuilders
          .queryFilter(
            d.mustTags.foldLeft(QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())) {
              case (acc, elem) => acc.must(QueryBuilders.matchQuery("tags", elem).operator(MatchQueryBuilder.Operator.AND))
            }),
        FilterBuilders
          .rangeFilter("price").from(d.priceMin.value).to(d.priceMax.value)
      )
    )
  }

  private[es] def buildMatchingScoreQuery(d: Demand) = {
    QueryBuilders.functionScoreQuery(
      d.shouldTags.foldLeft(QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())) {
        case (acc, elem) => acc.should(QueryBuilders.matchQuery("tags", elem))
      }
    ).add(
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

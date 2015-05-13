package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.sphere.{ProductTypes, SphereClient}
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.{PagedQueryResult, QueryDsl}
import model.{Card, Demand, Offer, OfferId}
import org.elasticsearch.action.search.{SearchRequestBuilder, SearchResponse}
import org.elasticsearch.index.query._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingService(sphereClient: SphereClient, esMatching: EsMatchingService,
                      productTypes: ProductTypes) {

  //TODO use from and size
  def matchDemand(from: From, pageSize: PageSize, demand: Demand): Future[MatchingResult] = {
    esMatching.matchOfferIdsFromEs(from, pageSize, demand).flatMap {
      esResult => {
        if (esResult.results.nonEmpty) {
          val predicate = ProductQuery.model().id().isIn(esResult.results.map(_.value).asJava)
          val sphereQuery = ProductQuery.of().byProductType(productTypes.offer).withPredicate(predicate)

          val offers = sphereClient.execute(sphereQuery) map {
            pageQueryResult =>
              pageQueryResult.getResults.asScala.toList.flatMap(Offer.fromProduct(_).toOption)
          }

          offers.map(MatchingResult(esResult.hits, from, pageSize, _))
        } else {
          Future.successful(MatchingResult(0L, from, pageSize, List.empty[Offer]))
        }
      }
    }
  }

  def matchDemands(): Future[List[Demand]] = {
    //TODO remove limit parameter with clever solution
    val query: QueryDsl[Product] = ProductQuery.of().byProductType(productTypes.demand).withLimit(500)
    val result: Future[PagedQueryResult[Product]] = sphereClient.execute(query)

    result.map(queryResult => queryResult.getResults.asScala.toList.map(Demand.fromProduct).flatten)
  }
}

class EsMatchingService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {
  def getShouldTagsQuery(shouldTags: Set[String]) : QueryBuilder = {
    if (shouldTags.isEmpty) new MatchAllQueryBuilder()
    else new TermsQueryBuilder("tags", shouldTags.asJava).minimumShouldMatch("10%")
  }

  def searchResponseToEsMatchingResult(searchResponse: SearchResponse): EsMatchingResult = {
    val list = searchResponse.getHits.getHits.map {
      hit => OfferId(hit.getId)
    }.toList
    val totalHits = searchResponse.getHits.getTotalHits

    EsMatchingResult(totalHits, list)
  }

  def getMustTagsFilter(mustTags: Set[String]): FilterBuilder =
    new TermsFilterBuilder("tags", mustTags.asJava)

  def matchOfferIdsFromEs(from: From, pageSize: PageSize, demand: Demand): Future[EsMatchingResult] = {
    val query = buildQuery(demand, from, pageSize)
    query.execute().asScala.map(searchResponseToEsMatchingResult)
  }

  def buildQuery(card: Card, from: From, pageSize: PageSize): SearchRequestBuilder = {
    card match {
      case d: Demand =>
        val indexName = config.offerIndex
        elasticsearch.client
          .prepareSearch(indexName.value)
          .setQuery(
            new FilteredQueryBuilder(
              getShouldTagsQuery(d.shouldTags),
              getMustTagsFilter(d.mustTags)
            )
          )
      case o: Offer =>
        //TODO match against offers
        val indexName = config.demandIndex
        elasticsearch.client
          .prepareSearch(indexName.value)
          .setQuery(getShouldTagsQuery(o.tags))
    }
  }
}
package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.helper.Configloader
import common.sphere.{ProductTypes, SphereClient}
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.{PagedQueryResult, QueryDsl}
import model.{Card, OfferId, Offer, Demand}
import org.elasticsearch.action.search.{SearchRequestBuilder, SearchResponse}
import org.elasticsearch.index.query._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.collection.JavaConverters._
import common.helper.ImplicitConversions.ActionListenableFutureConverter

class MatchingService(sphereClient: SphereClient, esMatching: EsMatchingService, productTypes: ProductTypes) {

  //TODO use from and size
  def matchDemand(from: From, pageSize: PageSize, demand: Demand): Future[MatchingResult] = {
    esMatching.matchOfferIdsFromEs(from, pageSize, demand).flatMap {
      esResult => {
        val predicate = ProductQuery.model().id().isIn(esResult.results.map(_.value).asJava)
        val sphereQuery = ProductQuery.of().byProductType(productTypes.offer).withPredicate(predicate)

        sphereClient.execute(sphereQuery).map {
          queryResult =>
            MatchingResult(
              esResult.hits,
              from,
              pageSize,
              queryResult.getResults.asScala.toList.map(Offer.productToOffer).flatten
            )
        }
      }
    }
  }

  def matchDemands(): Future[List[Demand]] = {
    //TODO remove limit parameter with clever solution
    val query: QueryDsl[Product] = ProductQuery.of().byProductType(productTypes.demand).withLimit(500)
    val result: Future[PagedQueryResult[Product]] = sphereClient.execute(query)

    result.map(queryResult => queryResult.getResults.asScala.toList.map(Demand.productToDemand).flatten)
  }
}

class EsMatchingService(elasticsearch: ElasticsearchClient) {
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
        val indexName = IndexName(Configloader.getString("offer.typeName"))
        val typeName = indexName.toTypeName
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
        val indexName = IndexName(Configloader.getString("demand.typeName"))
        val typeName = indexName.toTypeName
        elasticsearch.client
          .prepareSearch(indexName.value)
          .setQuery(getShouldTagsQuery(o.tags))
    }
  }
}
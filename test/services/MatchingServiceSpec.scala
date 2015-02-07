package services

import java.util.Locale

import common.domain._
import common.elasticsearch.{ElasticsearchClientFactory, ElasticsearchClient}
import common.sphere.{ProductTypeDrafts, MockProductTypes, SphereClient}
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products._
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.queries.PagedQueryResult
import model.{OfferId}
import org.elasticsearch.action.search.{ShardSearchFailure, SearchResponse}
import org.elasticsearch.common.bytes.{BytesArray, BytesReference}
import org.elasticsearch.common.text.StringText
import org.elasticsearch.index.query.{FilteredQueryBuilder, TermsFilterBuilder, TermsQueryBuilder, MatchAllQueryBuilder}
import org.elasticsearch.search.SearchShardTarget
import org.elasticsearch.search.internal.{InternalSearchHits, InternalSearchHit, InternalSearchResponse}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.{TestApplications, TestData}
import scala.collection.JavaConverters._
import scala.concurrent.Future

class MatchingServiceSpec extends Specification with Mockito {

  "MatchingService & EsMatchingService" should {
    val productVariant = ProductVariantBuilder.of(1).attributes(TestData.offerProductAttributeList).build()

    val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()
    val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method

    val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
    val offerProduct = ProductBuilder.of(productType, masterData).id(TestData.offerId.value).build()


    "getShouldTagsQuery must return matchAll Query for empty shouldTags" in {
      val elasticSearch = mock[ElasticsearchClient]
      val esMatchingService = new EsMatchingService(elasticSearch)

      esMatchingService.getShouldTagsQuery(Set.empty[String]).toString mustEqual new MatchAllQueryBuilder().toString
    }

    "getShouldTagsQuery must return terms Query for shouldTags with 10% minimum should match" in {
      val elasticSearch = mock[ElasticsearchClient]
      val esMatchingService = new EsMatchingService(elasticSearch)

      esMatchingService.getShouldTagsQuery(Set("bla", "blub")).toString mustEqual
        new TermsQueryBuilder("tags", Set("bla", "blub").asJava).minimumShouldMatch("10%").toString
    }

    "getMustTagFilter must return terms Filter for mustTags" in {
      val elasticSearch = mock[ElasticsearchClient]
      val esMatchingService = new EsMatchingService(elasticSearch)

      esMatchingService.getMustTagsFilter(Set("bla", "blub")).toString mustEqual
        new TermsFilterBuilder("tags", Set("bla", "blub").asJava).toString
    }

    "buildQuery must return valid SearchRequestBuilder for valid demands" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")) {

        val elasticSearch = ElasticsearchClientFactory.instance
        val esMatchingService = new EsMatchingService(elasticSearch)
        val from = From(0)
        val size = PageSize(0)

        esMatchingService.buildQuery(TestData.demand, from, size).toString mustEqual
          elasticSearch.client
            .prepareSearch("offer")
            .setQuery(
              new FilteredQueryBuilder(
                new TermsQueryBuilder("tags", TestData.demand.shouldTags.asJava).minimumShouldMatch("10%"),
                new TermsFilterBuilder("tags", TestData.demand.mustTags.asJava)
              )
            ).toString
      }

    "buildQuery must return valid SearchRequestBuilder for valid offers" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")) {

        val elasticSearch = ElasticsearchClientFactory.instance
        val esMatchingService = new EsMatchingService(elasticSearch)
        val from = From(0)
        val size = PageSize(0)

        esMatchingService.buildQuery(TestData.offer, from, size).toString mustEqual
          elasticSearch.client
            .prepareSearch("offer")
            .setQuery(new TermsQueryBuilder("tags", TestData.offer.tags.asJava).minimumShouldMatch("10%"))
            .toString
      }

    "SearchResponseToEsMatchingResult must return valid EsMatching object for searchresponse" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")) {
        val shardTarget: SearchShardTarget = new SearchShardTarget("1", "offer", 1)
        val shardFailures = ShardSearchFailure.EMPTY_ARRAY
        val source: BytesReference = new BytesArray("{}")
        val hit: InternalSearchHit = new InternalSearchHit(1, "docId", new StringText("offer"), null)
        hit.shardTarget(shardTarget)
        hit.sourceRef(source)
        val hits: Array[InternalSearchHit] = Array(hit)
        val internalSearchHits = new InternalSearchHits(hits, 1, 1.0F)
        val internalSearchResponse = new InternalSearchResponse(internalSearchHits, null, null, null, false, false)
        val searchResponse = new SearchResponse(internalSearchResponse, "scrollId", 1, 1, 1000, shardFailures)

        val elasticSearch = mock[ElasticsearchClient]
        val esMatchingService = new EsMatchingService(elasticSearch)

        esMatchingService.searchResponseToEsMatchingResult(searchResponse) mustEqual EsMatchingResult(1, List(OfferId("docId")))
      }

    "machtDemands must return valid MatchingResult" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")) {
        val from = From(0)
        val size = PageSize(10)

        val sphereClient = mock[SphereClient]
        val predicate = ProductQuery.model().id().isIn(List(TestData.offerId.value).asJava)
        val sphereQuery = ProductQuery.of().byProductType(MockProductTypes.offer).withPredicate(predicate)
        sphereClient.execute(sphereQuery) returns Future.successful(PagedQueryResult.of(offerProduct))

        val esMatchingService = mock[EsMatchingService]
        esMatchingService.matchOfferIdsFromEs(from, size, TestData.demand) returns
          Future.successful(EsMatchingResult(1L, List(TestData.offerId)))

        val matchingService = new MatchingService(sphereClient, esMatchingService, MockProductTypes)

        matchingService.matchDemand(from, size, TestData.demand) must
          beEqualTo(MatchingResult(1L, from, size, List(TestData.offer))).await
      }
  }
}
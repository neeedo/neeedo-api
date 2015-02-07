package services

import common.domain._
import common.elasticsearch.{ElasticsearchClientFactory, ElasticsearchClient}
import common.sphere.{MockProductTypes, SphereClient}
import model.{OfferId, Card}
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

class MatchingServiceSpec extends Specification with Mockito {

  "MatchingService" should {
    "getShouldTagsQuery must return matchAll Query for empty shouldTags" in {
      val sphereClient = mock[SphereClient]
      val elasticSearch = mock[ElasticsearchClient]
      val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)

      matchingService.getShouldTagsQuery(Set.empty[String]).toString mustEqual new MatchAllQueryBuilder().toString
    }

    "getShouldTagsQuery must return terms Query for shouldTags with 10% minimum should match" in {
      val sphereClient = mock[SphereClient]
      val elasticSearch = mock[ElasticsearchClient]
      val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)

      matchingService.getShouldTagsQuery(Set("bla", "blub")).toString mustEqual
        new TermsQueryBuilder("tags", Set("bla", "blub").asJava).minimumShouldMatch("10%").toString
    }

    "getMustTagFilter must return terms Filter for mustTags" in {
      val sphereClient = mock[SphereClient]
      val elasticSearch = mock[ElasticsearchClient]
      val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)

      matchingService.getMustTagsFilter(Set("bla", "blub")).toString mustEqual
        new TermsFilterBuilder("tags", Set("bla", "blub").asJava).toString
    }

    "buildQuery must return valid SearchRequestBuilder for valid demands" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")){

        val sphereClient = mock[SphereClient]
        val elasticSearch = ElasticsearchClientFactory.instance
        val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)
        val from = From(0)
        val size = PageSize(0)

        matchingService.buildQuery(TestData.demand, from, size).toString mustEqual
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
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")){

        val sphereClient = mock[SphereClient]
        val elasticSearch = ElasticsearchClientFactory.instance
        val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)
        val from = From(0)
        val size = PageSize(0)

        matchingService.buildQuery(TestData.offer, from, size).toString mustEqual
          elasticSearch.client
            .prepareSearch("offer")
            .setQuery(new TermsQueryBuilder("tags", TestData.offer.tags.asJava).minimumShouldMatch("10%"))
            .toString
      }

    "SearchResponseToEsMatchingResult must return valid EsMatching object for searchresponse" in
      TestApplications.loggingOffApp(Map("offer.typeName" -> "offer")){

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

        val sphereClient = mock[SphereClient]
        val elasticSearch = ElasticsearchClientFactory.instance
        val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)

        matchingService.searchResponseToEsMatchingResult(searchResponse) mustEqual EsMatchingResult(1, List(OfferId("docId")))
      }
  }
}

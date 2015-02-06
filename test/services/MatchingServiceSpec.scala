package services

import common.domain.{IndexName, MatchingResult, From, PageSize}
import common.elasticsearch.{ElasticsearchClientFactory, ElasticsearchClient}
import common.sphere.{MockProductTypes, SphereClient}
import model.Card
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{FilteredQueryBuilder, TermsFilterBuilder, TermsQueryBuilder, MatchAllQueryBuilder}
import org.elasticsearch.search.internal.InternalSearchResponse
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
      TestApplications.loggingOffApp(Map("offer.typeName" -> "demand")){

        val sphereClient = mock[SphereClient]
        val elasticSearch = ElasticsearchClientFactory.instance
        val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)
        val from = From(0)
        val size = PageSize(0)

        matchingService.buildQuery(TestData.offer, from, size).toString mustEqual
          elasticSearch.client
            .prepareSearch("demand")
            .setQuery(new TermsQueryBuilder("tags", TestData.offer.tags.asJava).minimumShouldMatch("10%"))
            .toString
      }
  }
}

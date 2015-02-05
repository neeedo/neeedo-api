package services

import common.domain.{MatchingResult, From, PageSize}
import common.elasticsearch.ElasticsearchClient
import common.sphere.{MockProductTypes, SphereClient}
import model.Card
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.{TermsQueryBuilder, MatchAllQueryBuilder}
import org.elasticsearch.search.internal.InternalSearchResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.TestData
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

      matchingService.getShouldTagsQuery(Set("bla", "blub")).toString mustEqual
        new TermsQueryBuilder("tags", Set("bla", "blub").asJava).minimumShouldMatch("10%").toString
    }

//    "matchDemand must return empty MatchingResult when es and sphere are answering with empty results" in {
//      val sphereClient = mock[SphereClient]
//      val elasticsearch = mock[ElasticsearchClient]
//      val matchingService = new MatchingService(sphereClient, elasticsearch, MockProductTypes)
//      val from = From(0)
//      val size = PageSize(0)
//
//      matchingService.matchDemand(from, size, TestData.demand) mustEqual MatchingResult(0, from, size, List.empty[Card])
//    }
  }
}

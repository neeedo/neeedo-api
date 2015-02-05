package services

import common.elasticsearch.ElasticsearchClient
import common.sphere.{MockProductTypes, SphereClient}
import org.elasticsearch.index.query.{TermsQueryBuilder, MatchAllQueryBuilder}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import scala.collection.JavaConverters._

class MatchingServiceSpec extends Specification with Mockito {

  "MatchingService" should {
//    "getShouldTagsQuery must return matchAll Query for empty shouldTags" in {
//      val sphereClient = mock[SphereClient]
//      val elasticSearch = mock[ElasticsearchClient]
//      val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)
//
//      matchingService.getShouldTagsQuery(Set.empty[String]) must be(new MatchAllQueryBuilder())
//    }
//
//    "getShouldTagsQuery must return terms Query for shouldTags with 10% minimum should match" in {
//      val sphereClient = mock[SphereClient]
//      val elasticSearch = mock[ElasticsearchClient]
//      val matchingService = new MatchingService(sphereClient, elasticSearch, MockProductTypes)
//
//      matchingService.getShouldTagsQuery(Set("bla", "blub")) must be(
//        new TermsQueryBuilder("tags", Set("bla", "blub").asJava).minimumShouldMatch("10%")
//      )
//    }
  }
}

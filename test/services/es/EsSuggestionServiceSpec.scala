package services.es

import common.domain.CompletionPhrase
import common.elasticsearch.ElasticsearchClient
import common.helper.ConfigLoader
import org.apache.lucene.util.BytesRef
import org.elasticsearch.action.search.{SearchResponse, ShardSearchFailure}
import org.elasticsearch.common.xcontent.{ToXContent, XContentBuilder, XContentFactory}
import org.elasticsearch.search.aggregations.bucket.significant.SignificantStringTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.significant.heuristics.ChiSquare
import org.elasticsearch.search.aggregations.bucket.significant.{InternalSignificantTerms, SignificantStringTerms}
import org.elasticsearch.search.aggregations.{InternalAggregation, InternalAggregations}
import org.elasticsearch.search.internal.InternalSearchResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.Json
import play.api.test.WithApplication

import scala.collection.JavaConverters._

class EsSuggestionServiceSpec extends Specification with Mockito {

  trait EsSuggestionServiceContext extends WithApplication with SearchResponseContext {
    val config = Map(
      "offer.typeName" -> "offer",
      "demand.typeName"-> "demand")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val completionPhrase = CompletionPhrase(List("bla", "blub"))

    val esMock = mock[ElasticsearchClient]
    val service = new EsSuggestionService(esMock, configLoader)
  }

  trait SearchResponseContext {
    val shardFailures = ShardSearchFailure.EMPTY_ARRAY

    val bytes: List[BytesRef] =
      List(new BytesRef("iphone"), new BytesRef("bla"), new BytesRef("blub"), new BytesRef("test"))

    val buckets: List[InternalSignificantTerms.Bucket] =
      bytes map(new Bucket(_,0L,0L,0L,0L, InternalAggregations.EMPTY))

    val aggregation: InternalAggregation =
      new SignificantStringTerms(0,0,"tags",0,0, new ChiSquare(false, false), buckets.asJavaCollection)

    val aggregations: InternalAggregations =
      new InternalAggregations(List(aggregation).asJava)

    val internalSearchResponse = new InternalSearchResponse(null, null, aggregations,
      null, false, false)

    val searchResponse = new SearchResponse(internalSearchResponse, "scrollId", 1, 1, 1000, shardFailures)
  }

  "EsSuggestionService" should {
    "calcShouldMatch must calculate the correct should amount" in new EsSuggestionServiceContext {
      val shouldMatches = List(1, 2, 2, 3, 3, 3, 3, 4, 4)
      val calcShouldMatches = (1 until 10) map service.calcShouldMatch toList

      calcShouldMatches must be equalTo shouldMatches
    }

    "buildPhraseCompletionQuery must return correct query" in new EsSuggestionServiceContext {
      val query = service.buildPhraseCompletionQuery(completionPhrase)
      Json.parse(query.toString) must be equalTo Json.parse("{\"bool\":{\"must\":{\"match_all\":{}},\"should\":[{\"match\":{\"completionTags\":{\"query\":\"bla\",\"type\":\"boolean\"}}},{\"match\":{\"completionTags\":{\"query\":\"blub\",\"type\":\"boolean\"}}}],\"minimum_should_match\":\"2\"}}")
    }

    "buildAggregation must return correct aggregation" in new EsSuggestionServiceContext {
      val builder: XContentBuilder = XContentFactory.jsonBuilder.prettyPrint()
      service.buildAggregation(completionPhrase).toXContent(builder, ToXContent.EMPTY_PARAMS)

      builder.string must be equalTo "\n\"tags\"{\n  \"significant_terms\" : {\n    \"field\" : \"completionTags\",\n    \"size\" : 20,\n    \"min_doc_count\" : 1,\n    \"exclude\" : [ \"bla\", \"blub\" ],\n    \"chi_square\" : {\n      \"include_negatives\" : false,\n      \"background_is_superset\" : false\n    }\n  }\n}"
    }

    "getBucketsFromSearchresponse must return correct result" in new EsSuggestionServiceContext {
      service.getBucketsFromSearchresponse(searchResponse, completionPhrase) must
        beEqualTo(List("iphone", "bla", "blub", "test"))
    }
  }
}

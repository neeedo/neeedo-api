package services.es

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import model.{Message, MessageId}
import org.elasticsearch.action.ListenableActionFuture
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.query._
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {

  def createMessage(message: Message): Future[Message] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName

    elasticsearch.indexDocument(message.id.value, index, typeName, buildEsMessageJson(message))
      .map(parseIndexResponse(_, message))
  }

  def getMessagesByUsers(u1: UserId, u2: UserId) = {
    elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(buildQuery(u1, u2))
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map { response =>
        elasticsearch.searchresponseAs[Message](response)
      }
  }

  case class Conversation(counterPart: User, lastMessage: Message, hasUnread: Boolean)

//  def getConversationsByUser(id: UserId) = {
//
//    val response = elasticsearch.client.prepareSearch(config.messagesIndex.value)
//      .setQuery(
//        QueryBuilders.filteredQuery(null,
//          FilterBuilders.andFilter(
//            FilterBuilders.termFilter("recipientId", id.value),
//            FilterBuilders.termFilter("read", false)
//          )
//        )
//      )
//      .addAggregation(
//        AggregationBuilders.terms("by_sender").field("senderId")
//      )
//      .execute().actionGet //blocking
//
//    val terms: Terms = response.getAggregations.get("by_sender")
//    val buckets = terms.getBuckets
//
//    buckets.asScala map {
//      bla => Conversation(
//        User(UserId(""), Version(1L), Username("bla"), Email("")),
//        Message(MessageId(""), UserId(""), UserId(""), "", 1L, read = false),
//        hasUnread = true
//      )
//    }
//    Future(buckets)
//  }

  def getConversationsByUser(id: UserId) = {
    elasticsearch.client.prepareSearch(config.messagesIndex.value)
      .setQuery(
        QueryBuilders.filteredQuery(null,
          FilterBuilders.andFilter(
            FilterBuilders.termFilter("recipientId", id.value),
            FilterBuilders.termFilter("read", false)
          )
        )
      )
      .addAggregation(
        AggregationBuilders.terms("by_sender").field("senderId")
      )
      .execute().asScala
      .map { res: SearchResponse =>
        val terms: Terms = res.getAggregations.get("by_sender")
        val buckets = terms.getBuckets

        buckets.asScala map {
          bucket => Conversation(
            User(UserId(""), Version(1L), Username("bla"), Email("")),
            Message(MessageId(""), UserId(""), UserId(""), "", 1L, read = false),
            hasUnread = true
          )
        }
      }
  }

  def markMessageRead(id: MessageId): Future[Option[MessageId]] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName

    elasticsearch.updateDocument(id.value, index, typeName)
      .map(_ => Option(id))
      .recover {
        case e: DocumentMissingException => Option.empty
      }
  }

  private[es] def parseIndexResponse(indexResponse: IndexResponse, message: Message): Message = {
      if (indexResponse.isCreated)
        message
      else
        throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def buildEsMessageJson(message: Message) = Json.toJson(message).as[JsObject]

  private[es] def buildQuery(u1: UserId, u2: UserId) = {
    val and1 = new AndFilterBuilder(
      new TermFilterBuilder("senderId", u1.value),
      new TermFilterBuilder("recipientId", u2.value))

    val and2 = new AndFilterBuilder(
      new TermFilterBuilder("senderId", u2.value),
      new TermFilterBuilder("recipientId", u1.value))

    val or = new OrFilterBuilder(and1, and2)
    new FilteredQueryBuilder(null, or)
  }

}

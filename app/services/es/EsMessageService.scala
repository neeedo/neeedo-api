package services.es

import java.util.UUID

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.helper.{ConfigLoader, TimeHelper}
import model.{Message, MessageId}
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.query._
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms
import org.elasticsearch.search.aggregations.bucket.significant.SignificantTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms.Bucket
import org.elasticsearch.search.aggregations.bucket.terms.{Terms, StringTerms}
import org.elasticsearch.search.aggregations.{Aggregation, AggregationBuilders}
import scala.collection.JavaConverters._
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.Json
import services.UserService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader, userService: UserService, timeHelper: TimeHelper) {

  def createMessage(draft: MessageDraft): Future[Message] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName
    val message: Future[Message] = for {
      recipient <- userService.getUserById(draft.recipientId)
      sender <- userService.getUserById(draft.senderId)
    } yield buildMessage(recipient, sender, draft)

    message.flatMap {
      mes =>
        elasticsearch
          .indexDocument(mes.id.value, index, typeName, Json.toJson(draft))
          .map(parseIndexResponse(_, mes))
    } recover {
      case e: Exception => throw new ElasticSearchIndexFailed("Could not index Message")
    }
  }

  def buildMessage(recipient: User, sender: User, draft: MessageDraft) = {
    Message(
      MessageId(UUID.randomUUID.toString),
      UserIdAndName(sender.id, sender.username),
      UserIdAndName(recipient.id, recipient.username),
      draft.body,
      timeHelper.now.getMillis,
      read = false)
  }

  def getMessagesByUsers(u1: UserId, u2: UserId) = {
    elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(buildGetMessagesQuery(u1, u2))
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

  def buildConversationsQuery(id: UserId, read: Boolean) = {
    val filter = FilterBuilders.orFilter(
      FilterBuilders.termFilter("recipient.id", id.value),
      FilterBuilders.termFilter("sender.id", id.value),
      FilterBuilders.termFilter("read", read)
    )

    QueryBuilders.filteredQuery(null, filter)
  }

  def getConversationsByUser(id: UserId, read: Boolean) = {
    elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(buildConversationsQuery(id, read))
      .addAggregation(AggregationBuilders.terms("sender").field("sender.id").exclude(id.value))
      .addAggregation(AggregationBuilders.terms("recipient").field("recipient.id").exclude(id.value))
      .execute()
      .asScala
      .map {
        response =>
          val ids = getUserIdsSearchresponse(response)
          val messages: List[Message] = elasticsearch.searchresponseAs[Message](response)
      }
  }

  private[es] def getUserIdsSearchresponse(res: SearchResponse): Set[String] = {
    val sender: List[String] = getStringListFromAggregation(res, "sender")
    val recipient: List[String] = getStringListFromAggregation(res, "recipient")

    sender.union(recipient).toSet
  }


  private[es] def getStringListFromAggregation(res: SearchResponse, name: String) =
    res.getAggregations.get[StringTerms](name).getBuckets.asScala.toList.map(_.getKey)

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

  private[es] def buildGetMessagesQuery(u1: UserId, u2: UserId) = {
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

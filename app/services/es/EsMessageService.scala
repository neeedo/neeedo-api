package services.es

import java.util.UUID

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.helper.{ConfigLoader, TimeHelper}
import model.{OfferId, Message, MessageId}
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.query._
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.Json
import services.UserService

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader, userService: UserService, timeHelper: TimeHelper) {

  def alertDemandsFor(offerId: OfferId): Future[List[Message]] = {
    val usersFuture = getPercolatedDemandIds(offerId)
    usersFuture.flatMap {
      users =>
        users.toString()
        Future.sequence {
          users.map {
            user =>
              val draft = MessageDraft(
                UserId("Neeedo"),
                user,
                offerId.value)
              createMessage(draft)
          }
        }
    } recover {
      case e: Exception =>
        List.empty
    }
  }

  private[es] def getPercolatedDemandIds(offerId: OfferId): Future[List[UserId]] = {
      elasticsearch.client.preparePercolate()
        .setIndices(config.offerIndex.value)
        .setGetRequest(
          new GetRequest(
            config.offerIndex.value,
            config.offerIndex.toTypeName.value,
            offerId.value))
        .setDocumentType(config.offerIndex.toTypeName.value)
        .execute()
        .asScala
        .flatMap {
          res =>
            Future.sequence {
              res.getMatches.toList.map {
                matchedDemand =>
                  elasticsearch.client
                    .prepareGet(
                      config.demandIndex.value,
                      config.demandIndex.toTypeName.value,
                      matchedDemand.getId.string()
                    )
                    .execute()
                    .asScala
                    .map {
                      resp =>
                        UserId((Json.parse(resp.getSourceAsString) \ "user" \ "id").as[String])
                    }
              }
            }
      }
  }

  def createMessage(draft: MessageDraft): Future[Message] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName
    val message: Future[Message] = for {
      recipient <- userService.getUserById(draft.recipientId)
      sender <- if (draft.senderId.value == "Neeedo") Future(UserIdAndName(draft.senderId, Username("Neeedo")))
        else userService.getUserById(draft.senderId)
    } yield buildMessage(recipient, sender, draft)

    message.flatMap {
      mes =>
        elasticsearch
          .indexDocument(mes.id.value, index, typeName, Json.toJson(mes))
          .map(parseIndexResponse(_, mes))
    } recover {
      case e: Exception => throw new ElasticSearchIndexFailed("Could not index Message")
    }
  }

  def buildMessage(recipient: UserIdAndName, sender: UserIdAndName, draft: MessageDraft) = {
    Message(
      MessageId(UUID.randomUUID.toString),
      sender,
      recipient,
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

  def buildConversationsQuery(id: UserId, read: Boolean) = {
    val filter = FilterBuilders.andFilter(
      FilterBuilders.termFilter("read", read),
      FilterBuilders.orFilter(
        FilterBuilders.termFilter("recipient.id", id.value),
        FilterBuilders.termFilter("sender.id", id.value)
      )
    )

    QueryBuilders.filteredQuery(null, filter)
  }

  def getConversationsByUser(id: UserId, read: Boolean): Future[Set[UserIdAndName]] = {
    val query = elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(buildConversationsQuery(id, read))
      .addAggregation(AggregationBuilders.terms("sender").field("sender.id").exclude(id.value))
      .addAggregation(AggregationBuilders.terms("recipient").field("recipient.id").exclude(id.value))

    query
      .execute()
      .asScala
      .flatMap {
        response =>
          val ids = getUserIdsSearchresponse(response)

          Future.sequence {
            ids.map {
              id =>
                if (id == "Neeedo") Future(UserIdAndName(UserId(id), Username("Neeedo")))
                else userService.getUserById(UserId(id)).map(u => UserIdAndName(u.id, u.name))
            }
          }
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
      new TermFilterBuilder("sender.id", u1.value),
      new TermFilterBuilder("recipient.id", u2.value))

    val and2 = new AndFilterBuilder(
      new TermFilterBuilder("sender.id", u2.value),
      new TermFilterBuilder("recipient.id", u1.value))

    val or = new OrFilterBuilder(and1, and2)
    new FilteredQueryBuilder(null, or)
  }

}

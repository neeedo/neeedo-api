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
import services.sphere.SphereUserService

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader, userService: SphereUserService, timeHelper: TimeHelper) {

  def alertDemandsFor(offerId: OfferId): Future[List[Message]] = {
    val usersFuture = getPercolatedDemandIds(offerId)
    usersFuture flatMap {
      users => Future.sequence {
        users map {
          user => createMessage(MessageDraft(UserId("Neeedo"), user, offerId.value))
        }
      }
    } recover {
      case e: Exception => List.empty
    }
  }

  def createMessage(draft: MessageDraft): Future[Message] = {
    val messageFuture = for {
      recipient <- getUser(draft.recipientId)
      sender <- getUser(draft.senderId)
    } yield buildMessage(recipient, sender, draft)

    messageFuture flatMap {
      message => elasticsearch
        .indexDocument(message.id.value, config.messagesIndex, config.messagesIndex.toTypeName, Json.toJson(message))
        .map(parseIndexResponse(_, message))
    } recover {
      case e: Exception => throw new ElasticSearchIndexFailed("Could not index Message")
    }
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

  def markMessageRead(id: MessageId): Future[Option[MessageId]] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName

    elasticsearch.updateDocument(id.value, index, typeName) map {
      _ => Option(id)
    } recover {
      case e: DocumentMissingException => Option.empty
    }
  }

  def getConversationsByUser(id: UserId, read: Boolean): Future[Set[UserIdAndName]] = {
    val query = elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(buildConversationsQuery(id, read))
      .addAggregation(AggregationBuilders.terms("sender").field("sender.id").exclude(id.value))
      .addAggregation(AggregationBuilders.terms("recipient").field("recipient.id").exclude(id.value))

    query.execute()
      .asScala
      .flatMap {
      response => Future.sequence {
        getUserIdsSearchresponse(response) map {
          id => getUser(UserId(id))
        }
      }
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
          res => Future.sequence {
            res.getMatches.toList map {
              matchedDemand => elasticsearch.client
                  .prepareGet(
                    config.demandIndex.value,
                    config.demandIndex.toTypeName.value,
                    matchedDemand.getId.string()
                  )
                  .execute()
                  .asScala
                  .map {
                    resp => UserId((Json.parse(resp.getSourceAsString) \ "user" \ "id").as[String])
                  }
            }
          }
      }
  }

  private[es] def getUser(userId: UserId) = {
    if (userId.value == "Neeedo") Future(UserIdAndName(userId, Username("Neeedo")))
    else userService.getUserById(userId) map {
      senderOpt => senderOpt.getOrElse(UserIdAndName(userId, Username("User deleted")))
    }
  }

  private[es] def buildMessage(recipient: UserIdAndName, sender: UserIdAndName, draft: MessageDraft) = {
    Message(MessageId(UUID.randomUUID.toString), sender, recipient, draft.body, timeHelper.now.getMillis, read = false)
  }

  private[es] def buildConversationsQuery(id: UserId, read: Boolean) = {
    val filter = FilterBuilders.andFilter(
      FilterBuilders.termFilter("read", read),
      FilterBuilders.orFilter(
        FilterBuilders.termFilter("recipient.id", id.value),
        FilterBuilders.termFilter("sender.id", id.value)
      )
    )

    QueryBuilders.filteredQuery(null, filter)
  }

  private[es] def getUserIdsSearchresponse(res: SearchResponse): Set[String] = {
    val sender: List[String] = getStringListFromAggregation(res, "sender")
    val recipient: List[String] = getStringListFromAggregation(res, "recipient")

    sender.union(recipient).toSet
  }


  private[es] def getStringListFromAggregation(res: SearchResponse, name: String) =
    res.getAggregations.get[StringTerms](name).getBuckets.asScala.toList.map(_.getKey)

  private[es] def parseIndexResponse(indexResponse: IndexResponse, message: Message): Message = {
      if (indexResponse.isCreated) message
      else throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
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

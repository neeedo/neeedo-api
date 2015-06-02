package services.es

import common.domain.{UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import model.{MessageId, Message}
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {

  def createMessage(message: Message): Future[Message] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName

    elasticsearch.indexDocument(message.id.value, index, typeName, buildEsMessageJson(message))
      .map(parseIndexResponse(_, message))
  }

  def getMessagesForUsers(u1: UserId, u2: UserId) = {
    elasticsearch.client
      .prepareSearch(config.messagesIndex.value)
      .setQuery(QueryBuilders.termsQuery("senderId", u1.value))
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map { response =>
        elasticsearch.searchresponseAs[Message](response)
      }
  }

  def markMessageRead(id: MessageId) = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName

    elasticsearch.updateDocument(id.value, index, typeName)
      .map(_ => id)
  }

  private[es] def parseIndexResponse(indexResponse: IndexResponse, message: Message): Message = {
      if (indexResponse.isCreated)
        message
      else
        throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def buildEsMessageJson(message: Message) = Json.toJson(message).as[JsObject]

}

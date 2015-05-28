package services.es

import common.domain.User
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ConfigLoader
import model.Message
import org.elasticsearch.action.index.IndexResponse
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class EsMessageService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {

  def createMessage(message: Message): Future[Message] = {
    val index = config.messagesIndex
    val typeName = config.messagesIndex.toTypeName
    elasticsearch.indexDocument(message.id.value, index, typeName, buildEsMessageJson(message))
      .map(parseIndexResponse(_, message))
  }

  def getMessagesByUserIds(user1: User, user2: User) = ???

  def updateMessage(message: Message) = ???

  private[es] def parseIndexResponse(indexResponse: IndexResponse, message: Message): Message = {
      if (indexResponse.isCreated)
        message
      else
        throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def buildEsMessageJson(message: Message) = Json.toJson(message).as[JsObject]

}

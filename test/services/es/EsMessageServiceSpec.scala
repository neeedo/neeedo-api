package services.es

import common.domain._
import common.elasticsearch.{EsSettings, EsMapping, TestEsClient, ElasticsearchClient}
import common.exceptions.ElasticSearchIndexFailed
import common.helper.{TimeHelper, ConfigLoader}
import model.MessageId
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest
import org.elasticsearch.index.engine.DocumentMissingException
import org.elasticsearch.index.shard.ShardId
import org.joda.time.{DateTimeZone, DateTime}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.test.WithApplication
import services.sphere.SphereUserService

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class EsMessageServiceSpec extends Specification with Mockito {

  trait EsMessageServiceContext extends WithApplication {
    val esMock = mock[ElasticsearchClient]
    val userServiceMock = mock[SphereUserService]
    val configLoader = new ConfigLoader(Configuration.from(Map("messagesIndexName" -> "messages")))
    val timeStamp1 = 1434272348072L
    val timeStamp2 = 1434272348084L
    val timeHelperMock = mock[TimeHelper]
    timeHelperMock.now returns new DateTime(DateTimeZone.forID("Europe/Berlin")).withMillis(timeStamp1) thenReturn new DateTime(DateTimeZone.forID("Europe/Berlin")).withMillis(timeStamp2)
    val service = new EsMessageService(esMock, configLoader, userServiceMock, new TimeHelper)
    val u1 = UserId("u1")
    val u2 = UserId("u2")
    val user1 = UserIdAndName(u1, Username("user1"))
    val user2 = UserIdAndName(u2, Username("user2"))
    userServiceMock.getUserById(u1) returns Future(Some(UserIdAndName(u1, Username("user1"))))
    userServiceMock.getUserById(u2) returns Future(Some(UserIdAndName(u2, Username("user2"))))

    val getMessageQueryJson = Json.obj(
      "filtered" -> Json.obj(
        "filter" -> Json.obj(
          "or" -> Json.obj(
            "filters" -> Json.arr(
              Json.obj(
                "and" -> Json.obj(
                  "filters" -> Json.arr(
                    Json.obj("term" -> Json.obj("sender.id" -> u1.value)),
                    Json.obj("term" -> Json.obj("recipient.id" -> u2.value))
                  )
                )
              ),
              Json.obj(
                "and" -> Json.obj(
                  "filters" -> Json.arr(
                    Json.obj("term" -> Json.obj("sender.id" -> u2.value)),
                    Json.obj("term" -> Json.obj("recipient.id" -> u1.value))
                  )
                )
              )
            )
          )
        )
      )
    )
    val messageDraft1 = MessageDraft(u1, u2, "Testmessage")
    val messageDraft2 = MessageDraft(u2, u1, "Zweite Message")
  }


  trait EsMessageServiceIntegrationContext extends WithApplication with EsMessageServiceContext {
    val esClient = new TestEsClient()
    val integrationService = new EsMessageService(esClient, configLoader, userServiceMock, timeHelperMock)
    val esCompletionServiceMock = mock[EsCompletionService]
    val offerSerivce = new EsOfferService(esClient, configLoader, esCompletionServiceMock, timeHelperMock)
    val demandService = new EsDemandService(esClient, configLoader, esCompletionServiceMock, timeHelperMock)
  }
  
  "EsMessageService" should {
    "buildQuery should return valid query for userIds" in new EsMessageServiceContext {
      Json.parse(service.buildGetMessagesQuery(u1, u2).toString) mustEqual getMessageQueryJson
    }

    "createMessage should return exception when es fails" in new EsMessageServiceContext {
      esMock.indexDocument(any[String], any[IndexName], any[TypeName], any[JsValue]) returns Future.failed(new Exception("bla"))

      Await.result(service.createMessage(messageDraft1), Duration.Inf) must throwA[ElasticSearchIndexFailed]
    }

    "createMessage should index message in elasticsearch" in new EsMessageServiceIntegrationContext {
      val indexRequest = esClient.buildIndexRequest(configLoader.messagesIndex, EsMapping(configLoader.messagesIndex.toTypeName, "migrations/messages-mapping.json"))
      Await.result(esClient.createIndex(configLoader.messagesIndex, indexRequest), Duration.Inf) must be equalTo true

      val message1 = Await.result(integrationService.createMessage(messageDraft1), Duration.Inf)
      message1.recipient must be equalTo user2
      message1.sender must be equalTo user1
      message1.body must be equalTo messageDraft1.body

      val message2 = Await.result(integrationService.createMessage(messageDraft2), Duration.Inf)
      message2.recipient must be equalTo user1
      message2.sender must be equalTo user2
      message2.body must be equalTo messageDraft2.body

      esClient.client.admin().indices()
        .refresh(new RefreshRequest(configLoader.messagesIndex.value)).actionGet()
      Await.result(integrationService.getMessagesByUsers(u1, u2), Duration.Inf) must be equalTo List(message2, message1)
    }

    "markMessageRead should mark messages as read in elasticsearch" in new EsMessageServiceIntegrationContext {
      val indexRequest = esClient.buildIndexRequest(configLoader.messagesIndex, EsMapping(configLoader.messagesIndex.toTypeName, "migrations/messages-mapping.json"))
      Await.result(esClient.createIndex(configLoader.messagesIndex, indexRequest), Duration.Inf) must be equalTo true

      val message1 = Await.result(integrationService.createMessage(messageDraft1), Duration.Inf)
      message1.recipient must be equalTo user2
      message1.sender must be equalTo user1
      message1.body must be equalTo messageDraft1.body
      message1.read must beFalse

      Await.result(integrationService.markMessageRead(message1.id), Duration.Inf) must be equalTo Some(message1.id)
      esClient.client.admin().indices()
        .refresh(new RefreshRequest(configLoader.messagesIndex.value)).actionGet()
      Await.result(integrationService.getMessagesByUsers(u1, u2), Duration.Inf) must be equalTo List(message1.copy(read = true))
    }

    "markMessageRead should return none when es fails" in new EsMessageServiceContext {
      esMock.updateDocument(any[String], any[IndexName], any[TypeName]) returns Future.failed(new DocumentMissingException(new ShardId("index", 1), "type", "id"))

      Await.result(service.markMessageRead(MessageId("123")), Duration.Inf) must be equalTo None
    }

    "getConversationsByUser must return correct conversations" in new EsMessageServiceIntegrationContext {
      val indexRequest = esClient.buildIndexRequest(configLoader.messagesIndex, EsMapping(configLoader.messagesIndex.toTypeName, "migrations/messages-mapping.json"))
      Await.result(esClient.createIndex(configLoader.messagesIndex, indexRequest), Duration.Inf) must be equalTo true

      val message = Await.result(integrationService.createMessage(messageDraft1), Duration.Inf)
      esClient.client.admin().indices()
        .refresh(new RefreshRequest(configLoader.messagesIndex.value)).actionGet()

      Await.result(integrationService.getConversationsByUser(u1, read = false), Duration.Inf) must be equalTo Set()
      Await.result(integrationService.getConversationsByUser(u2, read = false), Duration.Inf) must be equalTo Set(user1)
      Await.result(integrationService.getConversationsByUser(u1, read = true), Duration.Inf) must be equalTo Set(user2)
      Await.result(integrationService.getConversationsByUser(u2, read = true), Duration.Inf) must be equalTo Set()

      Await.result(integrationService.markMessageRead(message.id), Duration.Inf) must be equalTo Some(message.id)
      esClient.client.admin().indices().refresh(new RefreshRequest(configLoader.messagesIndex.value)).actionGet()

      Await.result(integrationService.getConversationsByUser(u1, read = false), Duration.Inf) must be equalTo Set()
      Await.result(integrationService.getConversationsByUser(u2, read = false), Duration.Inf) must be equalTo Set()
      Await.result(integrationService.getConversationsByUser(u1, read = true), Duration.Inf) must be equalTo Set(user2)
      Await.result(integrationService.getConversationsByUser(u2, read = true), Duration.Inf) must be equalTo Set(user1)

    }

    "alertDemandsFor must return send messages to users with matching demands for one offer" in new EsMessageServiceContext {

    }
  }
}

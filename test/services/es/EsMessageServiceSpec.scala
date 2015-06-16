package services.es

import common.domain.UserId
import common.elasticsearch.ElasticsearchClient
import common.helper.{TimeHelper, ConfigLoader}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication
import services.UserService

class EsMessageServiceSpec extends Specification with Mockito {

  trait EsMessageServiceContext extends WithApplication {
    val esMock = mock[ElasticsearchClient]
    val userServiceMock = mock[UserService]
    val config = mock[ConfigLoader]
    val service = new EsMessageService(esMock, config, userServiceMock, new TimeHelper)

    val u1 = UserId("u1")
    val u2 = UserId("u2")
  }
  
  "EsMessageService" should {

    "buildQuery should return valid query for userIds" in new EsMessageServiceContext {
      Json.parse(service.buildGetMessagesQuery(u1, u2).toString) must beEqualTo(

        Json.obj("filtered" -> Json.obj( "filter" -> Json.obj(
            "or" -> Json.obj(
              "filters" -> Json.arr(
                Json.obj(
                  "and" -> Json.obj(
                    "filters" -> Json.arr(
                      Json.obj("term" -> Json.obj("sender.id" -> u1.value)),
                      Json.obj("term" -> Json.obj("recipient.id" -> u2.value))
                    )
                )),
                Json.obj(
                  "and" -> Json.obj(
                    "filters" -> Json.arr(
                      Json.obj("term" -> Json.obj("sender.id" -> u2.value)),
                      Json.obj("term" -> Json.obj("recipient.id" -> u1.value))
                    )
                ))
              )
            )
        )))
      )
    }

  }
}

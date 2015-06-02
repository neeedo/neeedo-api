package services.es

import common.domain.UserId
import common.elasticsearch.ElasticsearchClient
import common.helper.ConfigLoader
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication

class EsMessageServiceSpec extends Specification with Mockito {

  "EsMessageService" should {

    "buildQuery should return valid query for userIds" in new EsMessageServiceContext {
      Json.parse(service.buildQuery(u1, u2).toString) must beEqualTo(

        Json.obj("filtered" -> Json.obj( "filter" -> Json.obj(
            "or" -> Json.obj(
              "filters" -> Json.arr(
                Json.obj(
                  "and" -> Json.obj(
                    "filters" -> Json.arr(
                      Json.obj("term" -> Json.obj("senderId" -> u1.value)),
                      Json.obj("term" -> Json.obj("recipientId" -> u2.value))
                    )
                )),
                Json.obj(
                  "and" -> Json.obj(
                    "filters" -> Json.arr(
                      Json.obj("term" -> Json.obj("senderId" -> u2.value)),
                      Json.obj("term" -> Json.obj("recipientId" -> u1.value))
                    )
                ))
              )
            )
        )))
      )
    }

  }

  trait EsMessageServiceContext extends WithApplication {
    val esMock = mock[ElasticsearchClient]
    val config = mock[ConfigLoader]
    val service = new EsMessageService(esMock, config)

    val u1 = UserId("u1")
    val u2 = UserId("u2")
  }

}

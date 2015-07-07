package services.es

import common.domain.{UserId, Favorite}
import common.elasticsearch.ElasticsearchClient
import common.helper.{UUIDHelper, ConfigLoader}
import model.OfferId
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication

class EsFavoriteServiceSpec extends Specification with Mockito {

  trait EsFavoriteServiceContext extends WithApplication {
    val es = mock[ElasticsearchClient]
    val config = mock[ConfigLoader]
    val uuid = new UUIDHelper
    val service = new EsFavoriteService(es, config, uuid)

    val favorite = Favorite(UserId(uuid.random), OfferId(uuid.random))

    val deleteFavoriteQueryJson = Json.obj(
      "bool" -> Json.obj(
        "must" -> Json.arr(
          Json.obj(
            "term" -> Json.obj(
              "userId" -> favorite.userId.value
            )
          ),
          Json.obj(
            "term" -> Json.obj(
              "offerId" -> favorite.offerId.value
            )
          )
        )
      )
    )
  }

  "EsFavoriteService.buildDeleteFavoriteQuery" should {
    "return valid query for favorite" in new EsFavoriteServiceContext {
      Json.parse(service.buildDeleteFavoriteQuery(favorite).toString) mustEqual deleteFavoriteQueryJson
    }
  }
}

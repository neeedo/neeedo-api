package services.es

import common.domain.UserId
import common.elasticsearch.ElasticsearchClient
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import model.OfferId
import org.elasticsearch.index.query.{AndFilterBuilder, QueryBuilders, TermFilterBuilder}
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.Future

class EsFavoriteService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {

  val index = config.favoritesIndex
  val typeName = config.favoritesIndex.toTypeName

  def addFavorite(userId: UserId, offerId: OfferId): Future[Option[OfferId]] = {
    val favorite = buildEsJson(offerId, userId)

    elasticsearch.indexDocument(offerId.value, index, typeName, favorite) map {
      res => if(res.isCreated) Option(offerId) else None
    } recover {
      case e: Exception => throw e
    }
  }

  def getFavoritesByUser(userId: UserId): Future[List[OfferId]] = {
    elasticsearch.client
      .prepareSearch(index.value)
      .setQuery(QueryBuilders.termQuery("userId", userId.value))
      .execute()
      .asScala
      .map (elasticsearch.searchresponseAs[OfferId])
  }

  def removeFavorite(userId: UserId, offerId: OfferId): Future[OfferId] = {
    elasticsearch.client
      .prepareDeleteByQuery(index.value)
      .setQuery(deleteByUserAndIdQuery(userId, offerId))
      .execute()
      .asScala
      .map (_ => offerId)
  }

  private[es] def buildEsJson(offerId: OfferId, userId: UserId): JsValue = Json.obj(
    "offerId" -> offerId.value,
    "userId" -> userId.value
  )

  private[es] def deleteByUserAndIdQuery(userId: UserId, offerId: OfferId) =
    QueryBuilders.filteredQuery(null,
      new AndFilterBuilder(
        new TermFilterBuilder("userId", userId),
        new TermFilterBuilder("offerId", offerId)
      )
    )
}

package services.es

import java.util.UUID

import common.domain.{Favorite, UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsFavoriteService(elasticsearch: ElasticsearchClient, config: ConfigLoader) {

  val index = config.favoritesIndex
  val typeName = config.favoritesIndex.toTypeName

  def addFavorite(favorite: Favorite): Future[Favorite] = {
    val uuid = UUID.randomUUID().toString

    elasticsearch.indexDocument(uuid, index, typeName, Json.toJson(favorite)) map {
      result =>
        if(result.isCreated) favorite
        else throw new ElasticSearchIndexFailed("Error while saving favorite in elasticsearch")
    }
  }

  def getFavoritesByUser(userId: UserId): Future[List[Favorite]] = {
    elasticsearch.client
      .prepareSearch(index.value)
      .setQuery(QueryBuilders.termQuery("userId", userId.value))
      .execute()
      .asScala
      .map (elasticsearch.searchresponseAs[Favorite])
  }

  def removeFavorite(favorite: Favorite): Future[Favorite] = {
    elasticsearch.client
      .prepareDeleteByQuery(index.value)
      .setQuery(deleteFavoriteQuery(favorite))
      .execute()
      .asScala
      .map (_ => favorite)
  }

  private[es] def deleteFavoriteQuery(favorite: Favorite) = QueryBuilders.boolQuery()
    .must(QueryBuilders.termQuery("userId", favorite.userId.value))
    .must(QueryBuilders.termQuery("offerId", favorite.offerId.value))
}

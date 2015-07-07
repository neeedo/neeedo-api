package services.es

import common.domain.{Favorite, UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.ElasticSearchIndexFailed
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.helper.{ConfigLoader, UUIDHelper}
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsFavoriteService(elasticsearch: ElasticsearchClient, config: ConfigLoader, uuid: UUIDHelper) {

  def addFavorite(favorite: Favorite): Future[Favorite] = {
    val index = config.favoritesIndex

    elasticsearch.indexDocument(uuid.random, index, index.toTypeName, Json.toJson(favorite)) map {
      result =>
        if(result.isCreated) favorite
        else throw new ElasticSearchIndexFailed("Error while saving favorite in elasticsearch")
    }
  }

  def getFavoritesByUser(userId: UserId): Future[List[Favorite]] = {
    elasticsearch.client
      .prepareSearch(config.favoritesIndex.value)
      .setQuery(QueryBuilders.termQuery("userId", userId.value))
      .execute()
      .asScala
      .map (elasticsearch.searchresponseAs[Favorite])
  }

  def removeFavorite(favorite: Favorite): Future[Favorite] = {
    elasticsearch.client
      .prepareDeleteByQuery(config.favoritesIndex.value)
      .setQuery(buildDeleteFavoriteQuery(favorite))
      .execute()
      .asScala
      .map (_ => favorite)
  }

  private[es] def buildDeleteFavoriteQuery(favorite: Favorite): BoolQueryBuilder = QueryBuilders.boolQuery()
    .must(QueryBuilders.termQuery("userId", favorite.userId.value))
    .must(QueryBuilders.termQuery("offerId", favorite.offerId.value))
}

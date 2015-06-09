package services.es

import common.domain.{Pager, CompletionTag, UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ElasticSearchDeleteFailed, ElasticSearchIndexFailed, ProductNotFound}
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.logger.OfferLogger
import model.{Offer, OfferId}
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsOfferService(elasticsearch: ElasticsearchClient, config: ConfigLoader, esCompletionService: EsCompletionService) {

  def getOffersByUserId(id: UserId): Future[List[Offer]] = {
     elasticsearch.client
       .prepareSearch(config.offerIndex.value)
       .setQuery(QueryBuilders.matchQuery("userId", id.value))
       .addSort("_timestamp", SortOrder.DESC)
       .execute()
       .asScala
       .map {
         response => elasticsearch.searchresponseAs[Offer](response)
       }
  }

  def getAllOffers(pager: Pager): Future[List[Offer]] = {
    elasticsearch.client
      .prepareSearch(config.offerIndex.value)
      .setFrom(pager.offset)
      .setSize(pager.limit)
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map {
      response => elasticsearch.searchresponseAs[Offer](response)
    }
  }

  def createOffer(offer: Offer): Future[Offer] = {
    def throwAndReportEsIndexFailed(e: Exception) = {
      OfferLogger.error(s"Offer: ${Json.toJson(offer)} could not be saved in Elasticsearch" +
        s" Exception: ${e.getMessage}")
      throw new ElasticSearchIndexFailed("Error while saving offer in elasticsearch")
    }

    val index = config.offerIndex
    val typeName = config.offerIndex.toTypeName
    elasticsearch.indexDocument(offer.id.value, index, typeName, buildEsOfferJson(offer))
      .map(parseIndexResponse(_, offer))
      .recover {
        case e: Exception => throwAndReportEsIndexFailed(e)
      }
  }

  def deleteOffer(id: OfferId): Future[OfferId] = {
    def throwAndLogEsDeleteFailed(e: Exception) = {
      OfferLogger.error(s"Offer with id: ${id.value} could not be deleted from Elasticsearch. " +
        s"Exception: ${e.getMessage}")
      throw new ElasticSearchDeleteFailed("Error while deleting offer from Elasticsearch")
    }

    val deleteResult = elasticsearch
      .deleteDocument(id.value, config.offerIndex, config.offerIndex.toTypeName)

    deleteResult map {
      isFound =>
        if (isFound) id
        else throw new ProductNotFound(s"No offer with id: ${id.value} found")
    } recover {
      case e: ProductNotFound => throw e
      case e: Exception => throwAndLogEsDeleteFailed(e)
    }
  }

  def deleteAllOffers() = {
    val pager = Pager(Integer.MAX_VALUE, 0)
    getAllOffers(pager) map {
      (offers: List[Offer]) => {
        offers map { (offer: Offer) => deleteOffer(offer.id) }
      }
    }
  }

  private[es] def parseIndexResponse(indexResponse: IndexResponse, offer: Offer) = {
    if (indexResponse.isCreated) {
      esCompletionService.upsertCompletions(offer.tags.map(CompletionTag).toList)
      offer
    } else throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def buildEsOfferJson(offer: Offer) = {
    Json.obj( "completionTags" -> offer.tags) ++ Json.toJson(offer).as[JsObject]
  }
}

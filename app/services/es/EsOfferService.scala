package services.es

import common.domain.{ExternalImage, CompletionTag, UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ElasticSearchDeleteFailed, ElasticSearchIndexFailed, ProductNotFound}
import common.helper.ConfigLoader
import common.logger.OfferLogger
import model.{Offer, OfferId}
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}
import services.EsCompletionService
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsOfferService(elasticsearch: ElasticsearchClient, config: ConfigLoader, esCompletionService: EsCompletionService) {
  def addImageToOffer(id: OfferId, image: ExternalImage): Future[Offer] = ???


  def getOffersByUserId(id: UserId): Future[List[Offer]] = {
     elasticsearch.client
       .prepareSearch(config.offerIndex.value)
       .setQuery(QueryBuilders.termQuery("userId", id.value))
       .addSort("_timestamp", SortOrder.DESC)
       .execute()
       .asScala
       .map {
         response => elasticsearch.searchresponseAs[Offer](response)
       }
   }

   def writeOfferToEs(offer: Offer): Future[Offer] = {
     def throwAndReportElasticSearchIndexFailed = {
       OfferLogger.error(s"Offer: ${Json.toJson(offer)} could not be saved in Elasticsearch")
       throw new ElasticSearchIndexFailed("Error while saving offer in elasticsearch")
     }

     val index = config.offerIndex
     val typeName = config.offerIndex.toTypeName
     elasticsearch.indexDocument(offer.id.value, index, typeName, buildEsOfferJson(offer)).map {
       indexResponse =>
         if (indexResponse.isCreated) {
           esCompletionService.writeCompletionsToEs(offer.tags.map(CompletionTag).toList)
           offer
         }
         else throwAndReportElasticSearchIndexFailed
     } recover {
       case e: Exception => throwAndReportElasticSearchIndexFailed
     }
   }

   def buildEsOfferJson(offer: Offer) = {
     Json.obj( "completionTags" -> offer.tags) ++ Json.toJson(offer).as[JsObject]
   }

   def deleteOfferFromElasticsearch(id: OfferId): Future[OfferId] = {
     def throwAndLogElasticSearchDeleteFailed = {
       OfferLogger.error(s"Offer with id: ${id.value} could not be deleted from Elasticsearch")
       throw new ElasticSearchDeleteFailed("Error while deleting offer from Elasticsearch")
     }

     elasticsearch.client
       .prepareDelete(config.offerIndex.value, config.offerIndex.toTypeName.value, id.value)
       .execute()
       .asScala
       .map {
       deleteReq =>
         if (deleteReq.isFound) id
         else throw new ProductNotFound(s"No offer with id: ${id.value} found")
     }.recover {
       case e: Exception => throwAndLogElasticSearchDeleteFailed
     }
   }
 }

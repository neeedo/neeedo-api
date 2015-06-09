package services.es

import common.domain.{Pager, CompletionTag, UserId}
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ElasticSearchDeleteFailed, ElasticSearchIndexFailed, ProductNotFound}
import common.helper.ConfigLoader
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.logger.DemandLogger
import model.{Demand, DemandId}
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsDemandService(elasticsearch: ElasticsearchClient, config: ConfigLoader, esCompletionService: EsCompletionService) {

  def getDemandsByUserId(id: UserId, pager: Pager): Future[List[Demand]] = {
    elasticsearch.client
      .prepareSearch(config.demandIndex.value)
      .setQuery(QueryBuilders.matchQuery("userId", id.value))
      .setFrom(pager.offset)
      .setSize(pager.limit)
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map {
      response => elasticsearch.searchresponseAs[Demand](response)
    }
  }

  def getAllDemands(pager: Pager): Future[List[Demand]] = {
    elasticsearch.client
      .prepareSearch(config.demandIndex.value)
      .setFrom(pager.offset)
      .setSize(pager.limit)
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map {
      response => elasticsearch.searchresponseAs[Demand](response)
    }
  }

  def createDemand(demand: Demand): Future[Demand] = {
    def throwAndReportEsIndexFailed(e: Exception) = {
      DemandLogger.error(s"Demand: ${Json.toJson(demand)} could not be saved in Elasticsearch" +
        s" Exception: ${e.getMessage}")
      throw new ElasticSearchIndexFailed("Error while saving demand in elasticsearch")
    }

    val index = config.demandIndex
    val typeName = config.demandIndex.toTypeName
    elasticsearch.indexDocument(demand.id.value, index, typeName, buildEsDemandJson(demand))
      .map(parseIndexResponse(_, demand))
      .recover {
      case e: Exception => throwAndReportEsIndexFailed(e)
    }
  }

  def deleteDemand(id: DemandId): Future[DemandId] = {
    def throwAndLogEsDeleteFailed(e: Exception) = {
      DemandLogger.error(s"Demand with id: ${id.value} could not be deleted from Elasticsearch. " +
        s"Exception: ${e.getMessage}")
      throw new ElasticSearchDeleteFailed("Error while deleting demand from Elasticsearch")
    }

    val deleteResult = elasticsearch
      .deleteDocument(id.value, config.demandIndex, config.demandIndex.toTypeName)

    deleteResult map {
      isFound =>
        if (isFound) id
        else throw new ProductNotFound(s"No demand with id: ${id.value} found")
    } recover {
      case e: ProductNotFound => throw e
      case e: Exception => throwAndLogEsDeleteFailed(e)
    }
  }

  def deleteAllDemands() = {
    val pager = Pager(Integer.MAX_VALUE, 0)
    getAllDemands(pager) map {
      (demands: List[Demand]) => {
        demands map { (demand: Demand) => deleteDemand(demand.id) }
      }
    }
  }

  private[es] def parseIndexResponse(indexResponse: IndexResponse, demand: Demand) = {
    if (indexResponse.isCreated) {
      esCompletionService.upsertCompletions(demand.mustTags.map(CompletionTag).toList)
      demand
    } else throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def buildEsDemandJson(demand: Demand) = {
    Json.obj( "completionTags" -> demand.mustTags) ++ Json.toJson(demand).as[JsObject]
  }
}

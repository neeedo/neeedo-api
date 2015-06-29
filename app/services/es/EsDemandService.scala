package services.es

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ElasticSearchDeleteFailed, ElasticSearchIndexFailed, ProductNotFound}
import common.helper.ImplicitConversions.ActionListenableFutureConverter
import common.helper.{ConfigLoader, TimeHelper}
import common.logger.DemandLogger
import model.{Demand, DemandId}
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.common.unit.DistanceUnit
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.{FilterBuilders, QueryBuilders}
import org.elasticsearch.search.sort.SortOrder
import play.api.libs.json.{JsObject, Json}
import scala.collection.JavaConverters._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EsDemandService(elasticsearch: ElasticsearchClient,
                      config: ConfigLoader,
                      esCompletionService: EsCompletionService,
                      timeHelper: TimeHelper) extends EsSort(timeHelper) {

  def getDemandsByUserId(id: UserId, pager: Pager): Future[List[Demand]] = {
    elasticsearch.client
      .prepareSearch(config.demandIndex.value)
      .setQuery(QueryBuilders.termQuery("user.id", id.value))
      .setFrom(pager.offset)
      .setSize(pager.limit)
      .addSort("_timestamp", SortOrder.DESC)
      .execute()
      .asScala
      .map {
      response => elasticsearch.searchresponseAs[Demand](response)
    }
  }

  def getAllDemands(pager: Pager, location: Option[Location]): Future[List[Demand]] = {
    elasticsearch.client
      .prepareSearch(config.demandIndex.value)
      .setQuery(buildFunctionScoredQuery(location))
      .setFrom(pager.offset)
      .setSize(pager.limit)
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
      .flatMap(processIndexResponse(_, demand))
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

    elasticsearch
      .deleteDocument(id.value, config.demandIndex, config.demandIndex.toTypeName)
      .flatMap {
        res =>
          if (res) {
            elasticsearch
              .deleteDocument(id.value, config.offerIndex, TypeName(".percolator"))
              .map(_ => id)
          } else throw new ProductNotFound(s"No demand with id: ${id.value} found")
    } recover {
      case e: ProductNotFound => throw e
      case e: Exception => throwAndLogEsDeleteFailed(e)
    }
  }

  def deleteAllDemands() = {
    val pager = Pager(Integer.MAX_VALUE, 0)
    getAllDemands(pager, None) map {
      (demands: List[Demand]) => {
        demands map { (demand: Demand) => deleteDemand(demand.id) }
      }
    }
  }

  private[es] def processIndexResponse(indexResponse: IndexResponse, demand: Demand) = {
    if (indexResponse.isCreated) {
      for {
        completion <- esCompletionService.upsertCompletions(demand.mustTags.map(CompletionTag(_)).toList)
        percolator <- createPercolatorDemand(demand)
      } yield demand
    } else throw new ElasticSearchIndexFailed("Elasticsearch IndexResponse is negative")
  }

  private[es] def createPercolatorDemand(demand: Demand): Future[Boolean] = {
    val indexName = config.offerIndex
    val query = buildPercolateQuery(demand)

    val doc = Json.obj("query" -> Json.parse(query.toString))

    elasticsearch
      .indexDocument(demand.id.value, indexName, TypeName(".percolator"), doc)
      .map(_.isCreated)
  }

  private[es] def buildPercolateQuery(demand: Demand) = {
    QueryBuilders.filteredQuery(
      if (demand.shouldTags.isEmpty) QueryBuilders.matchAllQuery()
      else QueryBuilders.termsQuery("tags", demand.shouldTags.map(_.toLowerCase).asJava),
      FilterBuilders.andFilter(
        FilterBuilders
          .geoDistanceFilter("location")
          .distance(demand.distance.value, DistanceUnit.KILOMETERS)
          .point(demand.location.lat.value, demand.location.lon.value),
        FilterBuilders
          .termsFilter("tags", demand.mustTags.map(_.toLowerCase).asJava)
          .execution("and"),
        FilterBuilders
          .rangeFilter("price").from(demand.priceMin.value).to(demand.priceMax.value)
      )
    )
  }

  private[es] def buildEsDemandJson(demand: Demand) = {
    Json.obj("completionTags" -> (demand.mustTags ++ demand.shouldTags),
      "createdAt" -> timeHelper.now) ++ Json.toJson(demand).as[JsObject]
  }
}

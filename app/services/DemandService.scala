package services

import java.util.Optional

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.helper.Configloader
import common.sphere.SphereClient
import io.sphere.sdk.products.commands.ProductCreateCommand
import io.sphere.sdk.producttypes.ProductType
import io.sphere.sdk.producttypes.commands.{ProductTypeDeleteByIdCommand, ProductTypeCreateCommand}
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import model.sphere.{DemandProductDraftSupplier, CardProductTypeDraft}
import model.{Demand, DemandId}
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class DemandService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient) {
  val demandIndex = IndexName("demands")
  val demandType = TypeName("demands")

  implicit def optionalToOption[T](opt: java.util.Optional[T]): Option[T] = {
    if (opt.isPresent) Some[T](opt.get())
    else Option.empty[T]
  }

  def getDemands: Future[JsValue] = getDemandsFromEs.map {
    hits => Json.obj("demands" -> hits.toSeq.map {
      hit => Json.parse(hit.sourceAsString())
    })
  }

  def getDemandsFromEs = {
    elasticsearch.search(demandIndex, demandType, QueryBuilders.matchAllQuery()).map(result => result.getHits.getHits)
  }

  def getDemandById(id: DemandId): Option[Demand] = ???

  def addDemand(demand: Demand): Future[AddDemandResult] = {
    for {
      es <- writeDemandToEs(demand)
      //TODO write data to sphere IO
    } yield es
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    elasticsearch.indexDocument(demandIndex, demandType, Json.toJson(demand)).map(response =>
      DemandSaved
    ).recover {
      case _ => DemandCouldNotBeSaved
    }
  }

  def writeDemandToSphere(demand: Demand): Future[String] = {
    val productTypeCommand = ProductTypeCreateCommand.of(new CardProductTypeDraft().get())

    val typeName = Configloader.getStringOpt("demand.typeName").get
    val queryResult: Future[PagedQueryResult[ProductType]] = sphereClient.execute(ProductTypeQuery.of().byName(typeName))
    val option: Future[Option[ProductType]] = queryResult.map(res => res.head())

    option.map {
      case Some(prodType: ProductType) => sphereClient.execute(ProductTypeDeleteByIdCommand.of(prodType))
      case None => throw new IllegalStateException("Missing Product Type!")
    }

    Future.successful("Bla")
  }


}

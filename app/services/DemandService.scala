package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.SphereClient
import io.sphere.sdk.products.commands.ProductCreateCommand
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand
import model.sphere.{DemandProductDraftSupplier, CardProductTypeDraft}
import model.{Demand, DemandId}
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class DemandService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient) {
  val demandIndex = IndexName("demands")
  val demandType = TypeName("demands")

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
    val productTypeCommand: ProductTypeCreateCommand = ProductTypeCreateCommand.of(new CardProductTypeDraft().get())

    for {
      prodType <- sphereClient.execute(productTypeCommand)
      product <- {
        val productTemplate = new DemandProductDraftSupplier(prodType, "test123").get()
        sphereClient.execute(ProductCreateCommand.of(productTemplate))
      }
    } yield "Bla"
  }
}

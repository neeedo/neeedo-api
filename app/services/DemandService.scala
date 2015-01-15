package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.SphereClient
import model.{Demand, DemandId}
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.OptionalToOptionConverter._

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
      sphere <- writeDemandToSphere(demand)
      es <- writeDemandToEs(demand) if sphere.getClass == classOf[DemandSaved]
    } yield {
      (es, sphere) match {
        case (es: DemandSaved, sp: DemandSaved) => DemandSaved(sp.id)
        case (es: DemandSaved, DemandSaveFailed) => DemandSaveEsFailed
        case (DemandSaveFailed, sp: DemandSaved) => DemandSaveSphereFailed
        case (DemandSaveFailed, DemandSaveFailed) => DemandSaveFailed
      }
    }
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    elasticsearch.indexDocument(demandIndex, demandType, Json.toJson(demand)).map {
      case response if response.isCreated => DemandSaved(demand.id)
      case _ => DemandSaveFailed
    }.recover {
      case _ => DemandSaveFailed
    }
  }

  def writeDemandToSphere(demand: Demand): Future[AddDemandResult] = {
//    val productTypeCommand: ProductTypeCreateCommand = ProductTypeCreateCommand.of(new CardProductTypeDraft().get())
//
//    for {
//      prodType <- sphereClient.execute(productTypeCommand)
//      product <- {
//        val productTemplate = new DemandProductDraftSupplier(prodType, "test123").get()
//        sphereClient.execute(ProductCreateCommand.of(productTemplate))
//      }
//    } yield "Bla"
//

    Future.successful(DemandSaveFailed)
  }
}

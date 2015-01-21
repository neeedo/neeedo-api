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

  def addDemand(demandDraft: DemandDraft): Future[Option[Demand]] = {
    for {
      demandOption <- writeDemandToSphere(demandDraft)
      es <- writeDemandToEs(demandOption.get) if demandOption.isDefined
    } yield demandOption
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    elasticsearch.indexDocument(demandIndex, demandType, Json.toJson(demand)).map {
      case response if response.isCreated => DemandSaved(demand.id)
      case _ => DemandSaveFailed
    }.recover {
      case _ => DemandSaveFailed
    }
  }

  def writeDemandToSphere(demandDraft: DemandDraft): Future[Option[Demand]] = {
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

    Future.successful(Option(Demand(DemandId("1"), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))))
  }
}

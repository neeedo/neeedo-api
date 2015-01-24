package services

import java.util.{Optional, Locale}

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.SphereClient
import io.sphere.sdk.attributes.{AttributeAccess, Attribute}
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import io.sphere.sdk.products.{ProductVariantDraftBuilder, ProductDraftBuilder}
import model.sphere.{ProductTypeDrafts, ProductTypes}
import model.{Demand, DemandId}
import org.elasticsearch.index.query.QueryBuilders
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import common.helper.OptionalToOptionConverter._

class DemandService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient) {
  val demandIndex = IndexName("demands")
  val demandType = TypeName("demands")

  def addDemand(demandDraft: DemandDraft): Future[Option[Demand]] = {
    for {
      demandOption <- writeDemandToSphere(demandDraft)
      es <- writeDemandToEs(demandOption.get) if demandOption.isDefined
    } yield demandOption
  }

  def getDemands: Future[JsValue] = getDemandsFromEs.map {
    hits => Json.obj("demands" -> hits.toSeq.map {
      hit => Json.parse(hit.sourceAsString())
    })
  }

  def getDemandById(id: DemandId): Future[Option[Demand]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      productOptional: Optional[Product] =>
        val option: Option[Product] = productOptional
        option match {
          case Some(product) => Some(productToDemand(product))
          case _ => Option.empty[Demand]
      }
    }
  }

  // Todo
  def updateDemand(id: DemandId, demandDraft: DemandDraft): Future[Option[Demand]] = {
    Future.successful(Option.empty[Demand])
    //  for {
    //    demandOption <- writeDemandToSphere(demandDraft)
    //    es <- writeDemandToEs(demandOption.get) if demandOption.isDefined
    //  } yield demandOption
  }

  def deleteDemand(id: DemandId):Future[Option[Product]] = {
    for {
      productOptional <- getProductById(id)
      deletedProduct <- {
        val option: Option[Product] = productOptional
        option match {
          case Some(product) => sphereClient.execute(ProductDeleteByIdCommand.of(product)).map(Some(_))
          case None => Future.successful(Option.empty[Product])
        }
      }
    } yield deletedProduct
  }

  def getAttribute(product: Product, name: String) =
    product.getMasterData.getStaged.getMasterVariant.getAttribute(name).get()

  def getProductById(id: DemandId): Future[Optional[Product]] =
    sphereClient.execute(ProductFetchById.of(id.value))

  def getDemandsFromEs =
    elasticsearch.search(demandIndex, demandType, QueryBuilders.matchAllQuery()).map(result => result.getHits.getHits)

  def productToDemand(product: Product): Demand = {
    Demand(
      DemandId(product.getId),
      UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
      getAttribute(product, "tags").getValue(AttributeAccess.ofString().attributeMapper()),
      Location(
        Longitude(getAttribute(product, "longitude").getValue(AttributeAccess.ofDouble().attributeMapper())),
        Latitude(getAttribute(product, "latitude").getValue(AttributeAccess.ofDouble().attributeMapper()))
      ),
      Distance(getAttribute(product, "distance").getValue(AttributeAccess.ofDouble().attributeMapper()).intValue()),
      // Todo Nullpointer case
      Price(getAttribute(product, "priceMin").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue()),
      Price(getAttribute(product, "priceMax").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue())
    )
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    elasticsearch.indexDocument(demandIndex, demandType, Json.toJson(demand)).map {
      case response if response.isCreated => DemandSaved(demand.id)
      case _ => DemandSaveFailed
    } recover {
      case _ => DemandSaveFailed
    }
  }

  def writeDemandToSphere(demandDraft: DemandDraft): Future[Option[Demand]] = {
    // TODO Produktname?
    val productName = LocalizedStrings.of(Locale.ENGLISH, demandDraft.tags)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(demandDraft.tags))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildDemandAttributes(demandDraft))
      .build()

    val productDraft = ProductDraftBuilder.of(ProductTypes.demand, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Option(productToDemand(product))
    } recover {
      case e: Exception => {
        throw e
        Logger.error(e.getMessage)
        Option.empty[Demand]
      }
    }
  }

}

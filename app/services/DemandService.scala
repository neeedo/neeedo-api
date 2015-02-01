package services

import java.util.concurrent.CompletionException
import java.util.{Optional, Locale}

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantDraftBuilder, ProductDraftBuilder, Product}
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import model.{Demand, DemandId}
import play.api.Logger
import play.api.libs.json.Json
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import common.helper.ImplicitConversions._


class DemandService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient, productTypes: ProductTypes) {

  def createDemand(demandDraft: DemandDraft): Future[Option[Demand]] = {
    for {
      demandOption <- writeDemandToSphere(demandDraft)
      es <- demandOption match {
        case Some(demand) => writeDemandToEs(demand)
        case None => Future.successful(DemandSaveFailed)
      }
    } yield {
      (demandOption, es) match {
        case (None, DemandSaveFailed) =>
          Logger.error("WriteDemandToSphere failed")
          None
        case (Some(demand), DemandSaveFailed) =>
          deleteDemand(demand.id, demand.version)
          Logger.error("WriteDemandToEs failed. Rollback.")
          None
        case _ =>
          demandOption
      }
    }
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    //TODO indexname und typename in config verankern?
    val demandIndex = IndexName("demands")
    val demandType = TypeName("demands")
    elasticsearch.indexDocument(demandIndex, demandType, Json.toJson(demand)).map {
      indexResponse => if (indexResponse.isCreated) DemandSaved
      else DemandSaveFailed
    } recover {
      case e: Exception => //throw e
        DemandSaveFailed
    }
  }

  def writeDemandToSphere(demandDraft: DemandDraft): Future[Option[Demand]] = {
    // TODO Produktname?
    val productName = LocalizedStrings.of(Locale.ENGLISH, demandDraft.tags)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(demandDraft.tags))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildDemandAttributes(demandDraft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.demand, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Demand.productToDemand(product)
    } recover {
      case e: Exception =>
        Logger.error(e.getMessage)
        Option.empty[Demand]
    }
  }

  def getDemandById(id: DemandId): Future[Option[Demand]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      productOptional: Optional[Product] =>
        val option: Option[Product] = productOptional
        option match {
          case Some(product) => Demand.productToDemand(product)
          case _ => Option.empty[Demand]
      }
    }
  }

  def updateDemand(demandId: DemandId, version: Version, demandDraft: DemandDraft): Future[Option[Demand]] = {
    for {
      createDemand <- createDemand(demandDraft)
      deleteOldDemand <- deleteDemand(demandId, version)
    } yield createDemand
  }


  def deleteDemand(demandId: DemandId, version: Version): Future[Option[Demand]] = {
    val product: Versioned[Product] = Versioned.of(demandId.value, version.value)
    sphereClient.execute(ProductDeleteByIdCommand.of(product)).map(Demand.productToDemand).recover {
      // TODO besseres exception matching
      case e: CompletionException => Option.empty[Demand]
      case e: Exception => throw e
    }
  }

  def getProductById(id: DemandId): Future[Optional[Product]] =
    sphereClient.execute(ProductFetchById.of(id.value))
}

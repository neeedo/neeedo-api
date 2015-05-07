package services

import java.util.concurrent.CompletionException
import java.util.{Optional, Locale}

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.{EsIndices, ElasticsearchClient}
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantDraftBuilder, ProductDraftBuilder, Product}
import io.sphere.sdk.products.commands.{ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import model.{Offer, Demand, DemandId}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import common.helper.ImplicitConversions._

import scala.util.Random


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

  def buildEsDemandJson(demand: Demand) = {
    val completionJson = Json.obj("completionTags" -> (demand.mustTags ++ demand.shouldTags))

    completionJson ++ Json.toJson(demand).as[JsObject]
  }

  def writeDemandToEs(demand: Demand): Future[AddDemandResult] = {
    elasticsearch.indexDocument(demand.id.value, EsIndices.demandIndexName, EsIndices.demandTypeName, buildEsDemandJson(demand)).map {
      indexResponse => if (indexResponse.isCreated) DemandSaved
      else DemandSaveFailed
    } recover {
      case e: Exception => //throw e
        DemandSaveFailed
    }
  }

  def writeDemandToSphere(demandDraft: DemandDraft): Future[Option[Demand]] = {
    val name  = DemandDraft.generateName(demandDraft) + " " + Random.nextInt(1000)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildDemandAttributes(demandDraft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.demand, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Demand.fromProduct(product)
    } recover {
      case e: Exception =>
        Logger.error(e.getMessage)
        Option.empty[Demand]
    }
  }

  def getDemandById(id: DemandId): Future[Option[Demand]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      case Some(product) => Demand.fromProduct(product)
      case _ => Option.empty[Demand]
    }
  }

  def updateDemand(demandId: DemandId, version: Version, demandDraft: DemandDraft): Future[Option[Demand]] = {
    for {
      createDemand <- createDemand(demandDraft)
      deleteOldDemand <- deleteDemand(demandId, version)
    } yield createDemand
  }

  def deleteDemand(demandId: DemandId, version: Version): Future[Option[Demand]] = {
    for {
    demandOption <- deleteDemandFromSphere(demandId: DemandId, version: Version)
      es <- demandOption match {
        case Some(demand) => deleteDemandFromEs(demandId)
        case None => Future.successful(false)
      }
    } yield {
      (demandOption, es) match {
        case (None, false) =>
          Logger.error("DeleteDemandFromSphere failed")
          None
        case (Some(demand), false) =>
          Logger.error(s"DeleteDemandFromEs with Id: ${demand.id} failed.")
          None
        case _ =>
          demandOption
      }
    }
  }

  def deleteDemandFromSphere(demandId: DemandId, version: Version): Future[Option[Demand]] = {
    val product: Versioned[Product] = Versioned.of(demandId.value, version.value)
    sphereClient.execute(ProductDeleteCommand.of(product)).map(Demand.fromProduct).recover {
      // TODO besseres exception matching
      case e: CompletionException => Option.empty[Demand]
      case e: Exception => throw e
    }
  }

  def deleteDemandFromEs(demandId: DemandId): Future[Boolean] =
    elasticsearch.deleteDocument(demandId.value, EsIndices.demandIndexName, EsIndices.demandTypeName)

  def getProductById(id: DemandId): Future[Option[Product]] =
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)
}

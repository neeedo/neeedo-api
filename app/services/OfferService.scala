package services

import java.util.concurrent.CompletionException
import java.util.{Locale, Optional}

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.helper.Configloader
import common.sphere.{AddImageCommand, ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.models.{Image, Versioned, LocalizedStrings}
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.commands.{ProductUpdateCommand, ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.products.{ProductUpdateScope, ProductDraftBuilder, ProductVariantDraftBuilder, Product}
import model.{OfferId, Offer}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.Logger
import common.helper.ImplicitConversions._

import scala.concurrent.Future
import scala.util.Random

class OfferService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient, productTypes: ProductTypes) {

  def createOffer(draft: OfferDraft): Future[Option[Offer]] = {
    for {
      offerOption <- writeOfferToSphere(draft)
      es <- offerOption match {
        case Some(offer) => writeOfferToEs(offer)
        case None => Future.successful(OfferSaveFailed)
      }
    } yield {
      (offerOption, es) match {
        case (None, OfferSaveFailed) =>
          Logger.error("WriteOfferToSphere failed")
          None
        case (Some(offer), OfferSaveFailed) =>
          deleteOffer(offer.id, offer.version)
          Logger.error("WriteOfferToEs failed. Rollback.")
          None
        case _ =>
          offerOption
      }
    }
  }

  def writeOfferToEs(offer: Offer): Future[AddOfferResult] = {
    val offerIndex = IndexName(Configloader.getString("offer.typeName"))
    val offerType = offerIndex.toTypeName

    elasticsearch.indexDocument(offer.id.value, offerIndex, offerType, Json.toJson(offer)).map {
      indexResponse => if (indexResponse.isCreated) OfferSaved
      else OfferSaveFailed
    } recover {
      case _ => OfferSaveFailed
    }
  }

  def writeOfferToSphere(draft: OfferDraft): Future[Option[Offer]] = {
    val name = OfferDraft.generateName(draft) + " " + Random.nextInt(1000)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildOfferAttributes(draft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Offer.fromProduct(product)
    } recover {
      case e: Exception =>
        Logger.error(e.getMessage)
        Option.empty[Offer]
    }
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      case Some(product) => Offer.fromProduct(product)
      case _ => Option.empty[Offer]
    }
  }

  def updateOffer(id: OfferId, version: Version, draft: OfferDraft): Future[Option[Offer]] = {
    createOffer(draft) map {
      case Some(offer) => {
        deleteOffer(id, version)
        Some(offer)
      }
      case None => None
    }
  }

  def deleteOffer(id: OfferId, version: Version): Future[Option[Offer]] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)
    sphereClient.execute(ProductDeleteCommand.of(product)) map {
      Offer.fromProduct
    } recover {
      // TODO enhance exception matching
      case e: CompletionException => Option.empty[Offer]
      case e: Exception => throw e
    }
  }

  def getProductById(id: OfferId): Future[Option[Product]] =
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)

  def addImageToOffer(id: OfferId, img: ExternalImage): Future[Option[Offer]] = {
    getProductById(id) flatMap {
      case Some(product) => {
        sphereClient.execute(AddImageCommand(product, img)) map Offer.fromProduct
      }
      case None => Future(None)
    }
  }
}

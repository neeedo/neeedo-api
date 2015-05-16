package services.sphere

import java.util.Locale

import com.github.slugify.Slugify
import common.domain.{ExternalImage, Version, OfferDraft}
import common.exceptions.{ProductNotFound, SphereIndexFailed}
import common.logger.OfferLogger
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.commands.{ProductUpdateCommand, ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.products._
import model.{OfferId, Offer}
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.util.{Failure, Success, Random}
import common.helper.ImplicitConversions.OptionConverter
import scala.concurrent.ExecutionContext.Implicits.global

class SphereOfferService(sphereClient: SphereClient, productTypeDrafts: ProductTypeDrafts, productTypes: ProductTypes) {
  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)

    sphereClient.execute(ProductDeleteCommand.of(product)) map {
      // TODO Exception Nullpointer
      Offer.fromProduct(_).get
    }
  }

  def createOffer(draft: OfferDraft): Future[Offer] = {
    def throwAndReportSphereIndexFailed(e: Exception) = {
      OfferLogger.error(s"Offer: ${Json.toJson(draft)} could not be saved in Sphere. " +
        s"Exception: ${e.getMessage}")
      throw new SphereIndexFailed("Error while saving offer in sphere")
    }

    def buildProductDraft  = {
      val name = OfferDraft.generateName(draft) + " " + Random.nextInt(1000)
      val productName = LocalizedStrings.of(Locale.ENGLISH, name)
      val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
      val productVariant = ProductVariantDraftBuilder.of()
        .attributes(productTypeDrafts.buildOfferAttributes(draft))
        .build()

      ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()
    }

    sphereClient.execute(ProductCreateCommand.of(buildProductDraft)).map {
      product => Offer.fromProduct(product).get
    } recover {
      case e: Exception => throwAndReportSphereIndexFailed(e)
    }
  }

  private def getProductById(id: OfferId): Future[Option[Product]] = {
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    getProductById(id) map {
      case Some(product) => Offer.fromProduct(product).toOption
      case None => None
    }
  }

  def addImageToOffer(id: OfferId, img: ExternalImage): Future[Offer] = {
    getProductById(id) flatMap {
      case Some(product) =>
        sphereClient.execute(buildAddImageCommand(product, img)) map Offer.fromProduct map {
          case Success(offer) => offer
          case Failure(e) => throw new IllegalArgumentException(s"Product with id: ${id.value} is no valid offer")
        }
      case None => throw new ProductNotFound(s"No product with id: ${id.value} found")
    }
  }

  private def buildAddImageCommand(product: Product, image: ExternalImage) = {
    val sphereImage = ExternalImage.toSphereImage(image)
    val variantId = product.getMasterData.getStaged.getMasterVariant.getId
    val updateScope = ProductUpdateScope.STAGED_AND_CURRENT

    ProductUpdateCommand.of(product, AddExternalImage.of(sphereImage, variantId, updateScope))
  }

}

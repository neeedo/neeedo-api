package services.sphere

import java.util.Locale

import com.github.slugify.Slugify
import common.domain.{ExternalImage, OfferDraft, Version}
import common.exceptions.{SphereDeleteFailed, ProductNotFound, SphereIndexFailed}
import common.helper.ImplicitConversions.OptionConverter
import common.logger.OfferLogger
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, Versioned}
import io.sphere.sdk.products._
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.commands.{ProductCreateCommand, ProductDeleteCommand, ProductUpdateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.utils.MoneyImpl
import model.{Offer, OfferId}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}


class SphereOfferService(sphereClient: SphereClient, productTypeDrafts: ProductTypeDrafts, productTypes: ProductTypes) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    def throwAndReportSphereIndexFailed(e: Exception) = {
      OfferLogger.error(s"Offer: ${Json.toJson(draft)} could not be saved in Sphere. " +
        s"Exception: ${e.getMessage}")
      throw new SphereIndexFailed("Error while saving offer in sphere")
    }

    sphereClient.execute(ProductCreateCommand.of(buildProductDraft(draft))).map {
      product => Offer.fromProduct(product).get
    } recover {
      case e: Exception => throwAndReportSphereIndexFailed(e)
    }
  }

  private[sphere] def buildProductDraft(draft: OfferDraft)  = {
    val name = OfferDraft.generateName(draft)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(buildOfferAttributes(draft))
      .build()

    ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()
  }

  private[sphere] def buildOfferAttributes(offerDraft: OfferDraft) = List(
    Attribute.of("userId", offerDraft.uid.value),
    Attribute.of("tags", offerDraft.tags.mkString(";")),
    Attribute.of("longitude", offerDraft.location.lon.value),
    Attribute.of("latitude", offerDraft.location.lat.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(offerDraft.price.value).bigDecimal, "EUR"))
  ).asJava

  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)

    sphereClient.execute(ProductDeleteCommand.of(product)) map {
      // TODO Exception Nullpointer
      Offer.fromProduct(_).get
    } recover {
      case e: Exception => throw new SphereDeleteFailed("Offer could not be deleted")
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

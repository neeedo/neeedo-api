package services

import java.util.concurrent.CompletionException
import java.util.Locale
import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{SphereIndexFailed, ElasticSearchIndexFailed, ProductNotFound}
import common.helper.Configloader
import common.helper.ImplicitConversions._
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.commands.{ProductUpdateCommand, ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.products.{ProductUpdateScope, ProductDraftBuilder, ProductVariantDraftBuilder, Product}
import model.{OfferId, Offer}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.util.{Success, Random, Failure}

class OfferService(elasticsearch: ElasticsearchClient, sphereClient: SphereClient, productTypes: ProductTypes) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    writeOfferToSphere(draft).flatMap {
      offer => writeOfferToEs(offer).recoverWith {
        case e: Exception =>
          deleteOffer(offer.id, offer.version)
          throw e
      }
    }
  }

  def writeOfferToEs(offer: Offer): Future[Offer] = {
    val offerIndex = IndexName(Configloader.getString("offer.typeName"))
    val offerType = offerIndex.toTypeName

    elasticsearch.indexDocument(offer.id.value, offerIndex, offerType, Json.toJson(offer)).map {
      indexResponse =>
        if (indexResponse.isCreated) offer
        else throw new ElasticSearchIndexFailed("Error while saving offer in elasticsearch")
    } recover {
      case e: Exception => throw new ElasticSearchIndexFailed("Error while saving offer in elasticsearch")
    }
  }

  def writeOfferToSphere(draft: OfferDraft): Future[Offer] = {
    val name = OfferDraft.generateName(draft) + " " + Random.nextInt(1000)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildOfferAttributes(draft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Offer.fromProduct(product).get
    } recover {
      case e: Exception => throw new SphereIndexFailed("Error while saving offer in sphere")
    }
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      case Some(product) => Offer.fromProduct(product).toOption
      case None => None
    }
  }

  def updateOffer(id: OfferId, version: Version, draft: OfferDraft): Future[Offer] = {
    createOffer(draft) map {
      offer =>
        deleteOffer(id, version)
        offer
    }
  }

  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)
    sphereClient.execute(ProductDeleteCommand.of(product)) map {
      Offer.fromProduct(_).get
    } recover {
      case e: Exception => throw e
    }
  }

  def getProductById(id: OfferId): Future[Option[Product]] =
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)

  def addImageToOffer(id: OfferId, img: Image): Future[Offer] = {
    getProductById(id) flatMap {
      case Some(product) =>
        sphereClient.execute(buildAddImageCommand(product, img)) map Offer.fromProduct map {
          case Success(offer) => offer
          case Failure(e) => throw new IllegalArgumentException(s"Product with id: ${id.value} is no valid offer")
        }
      case None => throw new ProductNotFound(s"No product with id: ${id.value} found")
    }
  }

  def buildAddImageCommand(product: Product, image: Image) = {
    val sphereImage = Image.toSphereImage(image)
    val variantId = product.getMasterData.getStaged.getMasterVariant.getId
    val updateScope = ProductUpdateScope.STAGED_AND_CURRENT

    ProductUpdateCommand.of(product, AddExternalImage.of(sphereImage, variantId, updateScope))
  }
}

package services

import java.util.Locale

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{ElasticSearchDeleteFailed, ElasticSearchIndexFailed, ProductNotFound, SphereIndexFailed}
import common.helper.ConfigLoader
import common.helper.ImplicitConversions._
import common.logger.OfferLogger
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.models.{LocalizedStrings, Versioned}
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.commands.{ProductCreateCommand, ProductDeleteCommand, ProductUpdateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.products.{Product, ProductDraftBuilder, ProductUpdateScope, ProductVariantDraftBuilder}
import model.{Offer, OfferId}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future
import scala.util.{Failure, Random, Success}

class OfferService(sphereClient: SphereClient, productTypeDrafts: ProductTypeDrafts,
                   productTypes: ProductTypes, esOfferService: EsOfferService) {

  def createOffer(draft: OfferDraft): Future[Offer] = {
    writeOfferToSphere(draft).flatMap {
      offer => esOfferService.writeOfferToEs(offer).recoverWith {
        case e: Exception =>
          deleteOffer(offer.id, offer.version)
          throw e
      }
    }
  }

  def writeOfferToSphere(draft: OfferDraft): Future[Offer] = {
    def throwAndReportSphereIndexFailed = {
      OfferLogger.error(s"Offer: ${Json.toJson(draft)} could not be saved in Sphere")
      throw new SphereIndexFailed("Error while saving offer in sphere")
    }

    val name = OfferDraft.generateName(draft) + " " + Random.nextInt(1000)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(productTypeDrafts.buildOfferAttributes(draft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Offer.fromProduct(product).get
    } recover {
      case e: Exception => throwAndReportSphereIndexFailed
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
    esOfferService.deleteOfferFromElasticsearch(id).flatMap {
      offerId => val product: Versioned[Product] = Versioned.of(offerId.value, version.value)
      sphereClient.execute(ProductDeleteCommand.of(product)) map {
        Offer.fromProduct(_).get
      } recover {
        case e: Exception => throw e
      }
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

class EsOfferService(elasticsearch: ElasticsearchClient, config: ConfigLoader, esCompletionService: EsCompletionService) {
  def writeOfferToEs(offer: Offer): Future[Offer] = {
    def throwAndReportElasticSearchIndexFailed = {
      OfferLogger.error(s"Offer: ${Json.toJson(offer)} could not be saved in Elasticsearch")
      throw new ElasticSearchIndexFailed("Error while saving offer in elasticsearch")
    }

    val index = config.offerIndex
    val typeName = config.offerIndex.toTypeName
    elasticsearch.indexDocument(offer.id.value, index, typeName, buildEsOfferJson(offer)).map {
      indexResponse =>
        if (indexResponse.isCreated) {
          esCompletionService.writeCompletionsToEs(offer.tags.map(CompletionTag).toList)
          offer
        }
        else throwAndReportElasticSearchIndexFailed
    } recover {
      case e: Exception => throwAndReportElasticSearchIndexFailed
    }
  }

  def buildEsOfferJson(offer: Offer) = {
    Json.obj( "completionTags" -> offer.tags) ++ Json.toJson(offer).as[JsObject]
  }

  def deleteOfferFromElasticsearch(id: OfferId): Future[OfferId] = {
    def throwAndLogElasticSearchDeleteFailed = {
      OfferLogger.error(s"Offer with id: ${id.value} could not be deleted from Elasticsearch")
      throw new ElasticSearchDeleteFailed("Error while deleting offer from Elasticsearch")
    }

    elasticsearch.client
      .prepareDelete(config.offerIndex.value, config.offerIndex.toTypeName.value, id.value)
      .execute()
      .asScala
      .map {
      deleteReq =>
        if (deleteReq.isFound) id
        else throw new ProductNotFound(s"No offer with id: ${id.value} found")
    }.recover {
      case e: Exception => throwAndLogElasticSearchDeleteFailed
    }
  }
}

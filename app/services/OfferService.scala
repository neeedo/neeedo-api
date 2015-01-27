package services

import java.util.concurrent.CompletionException
import java.util.{Locale, Optional}

import com.github.slugify.Slugify
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import io.sphere.sdk.products.{ProductDraftBuilder, ProductVariantDraftBuilder, Product}
import model.{OfferId, Offer}
import org.elasticsearch.index.query.QueryBuilders
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsValue, Json}
import play.api.Logger
import common.helper.ImplicitConversions._

import scala.concurrent.Future

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
    //TODO indexname und typename in config verankern?
    val offerIndex = IndexName("offers")
    val offerType = TypeName("offers")
    elasticsearch.indexDocument(offerIndex, offerType, Json.toJson(offer)).map {
      _.isCreated match {
        case true => OfferSaved
        case false => OfferSaveFailed
      }
    } recover {
      case _ => OfferSaveFailed
    }
  }

  def writeOfferToSphere(draft: OfferDraft): Future[Option[Offer]] = {
    // TODO Produktname?
    val productName = LocalizedStrings.of(Locale.ENGLISH, draft.tags)
    val slug = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(draft.tags))
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(ProductTypeDrafts.buildOfferAttributes(draft))
      .build()

    val productDraft = ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()

    sphereClient.execute(ProductCreateCommand.of(productDraft)).map {
      product => Option(productToOffer(product))
    } recover {
      case e: Exception =>
        Logger.error(e.getMessage)
        Option.empty[Offer]
    }
  }

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    val futureProductOption = getProductById(id)

    futureProductOption.map {
      productOptional: Optional[Product] =>
        val option: Option[Product] = productOptional
        option match {
          case Some(product) => Some(productToOffer(product))
          case _ => Option.empty[Offer]
        }
    }
  }

  def updateOffer(id: OfferId, version: Version, draft: OfferDraft): Future[Option[Offer]] = {
    for {
      createOffer <- createOffer(draft)
      deleteOldOffer <- deleteOffer(id, version)
    } yield createOffer
  }

  def deleteOffer(id: OfferId, version: Version): Future[Option[Product]] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)
    sphereClient.execute(ProductDeleteByIdCommand.of(product)).map(Some(_)).recover {
      // TODO enhance exception matching
      case e: CompletionException => Option.empty[Product]
      case e: Exception => throw e
    }
  }

  // Todo Not specific to demand or offer, move to arbitrary location
  def getAttribute(product: Product, name: String) =
    product.getMasterData.getStaged.getMasterVariant.getAttribute(name).get()

  def getProductById(id: OfferId): Future[Optional[Product]] =
    sphereClient.execute(ProductFetchById.of(id.value))

  def productToOffer(product: Product): Offer = {
    Offer(
      OfferId(product.getId),
      Version(product.getVersion),
      UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
      getAttribute(product, "tags").getValue(AttributeAccess.ofString().attributeMapper()),
      Location(
        Longitude(getAttribute(product, "longitude").getValue(AttributeAccess.ofDouble().attributeMapper())),
        Latitude(getAttribute(product, "latitude").getValue(AttributeAccess.ofDouble().attributeMapper()))
      ),
      // Todo Nullpointer case
      Price(getAttribute(product, "price").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue())
    )
  }

//  def getOffers: Future[JsValue] = getOffersFromEs.map {
//    hits => Json.obj("offers" -> hits.toSeq.map {
//      hit => Json.parse(hit.sourceAsString())
//    })
//  }

//  def getOffersFromEs = {
//    elasticsearch.search(offerIndex, offerType, QueryBuilders.matchAllQuery()).map(result => result.getHits.getHits)
//  }

}

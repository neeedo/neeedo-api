package services.sphere

import java.util.Locale

import common.domain.{OfferDraft, Username, Version}
import common.exceptions.{MalformedOffer, SphereDeleteFailed, SphereIndexFailed}
import common.helper.ImplicitConversions.OptionConverter
import common.logger.OfferLogger
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, Versioned}
import io.sphere.sdk.products._
import io.sphere.sdk.products.commands.{ProductCreateCommand, ProductDeleteCommand}
import io.sphere.sdk.products.queries.{ProductByIdFetch, ProductQuery}
import io.sphere.sdk.utils.MoneyImpl
import model.{CardId, Offer, OfferId}
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class SphereOfferService(sphereClient:      SphereClient,
                         productTypeDrafts: ProductTypeDrafts,
                         productTypes:      ProductTypes,
                         userService:       SphereUserService) {

  def getOfferById(id: OfferId): Future[Option[Offer]] = {
    getProductById(id) map {
      case Some(product) => Offer.fromProduct(product).toOption
      case None => None
    }
  }

  def getOffersByIds(ids: List[OfferId]): Future[List[Offer]] = {
    val predicate = ProductQuery.model().id().isIn(ids.map(_.value).asJava)
    val query = ProductQuery.of().byProductType(productTypes.offer).withPredicate(predicate)

    sphereClient.execute(query) map {
      result =>
        result.getResults.asScala.toList.flatMap(o => Offer.fromProduct(o).toOption)
    }
  }

  def getAllOffers: Future[List[Product]] = {
    val productQuery = ProductQuery.of().byProductType(productTypes.offer).withLimit(500)

    sphereClient.execute(productQuery) map {
      res => res.getResults.asScala.toList
    }
  }

  def createOffer(draft: OfferDraft): Future[Offer] = {
    def throwAndReportSphereIndexFailed(e: Exception) = {
      OfferLogger.error(s"Offer: ${Json.toJson(draft)} could not be saved in Sphere. " +
        s"Exception: ${e.getMessage}")
      throw new SphereIndexFailed("Error while saving offer in sphere")
    }

    userService.getUserById(draft.uid).flatMap {
      user =>
        val productCreateCommand = ProductCreateCommand.of(buildProductDraft(user.name, draft))
        sphereClient.execute(productCreateCommand) map {
            product => Offer.fromProduct(product).get
        }
    } recover {
      case e: Exception => throwAndReportSphereIndexFailed(e)
    }
  }

  def deleteOffer(id: OfferId, version: Version): Future[Offer] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)

    deleteProduct(product) map {
      Offer.fromProduct(_)
        .getOrElse(throw new MalformedOffer("Could not create Offer from Product"))
    }
  }

  def deleteProduct(product: Versioned[Product]) = {
    sphereClient.execute(ProductDeleteCommand.of(product)) recover {
      case e: Exception => throw new SphereDeleteFailed("Offer could not be deleted")
    }
  }

  def deleteAllOffers() = {
    getAllOffers map {
      offerProducts => offerProducts map {
        product => deleteProduct(Versioned.of(product.getId, product.getVersion))
      }
    }
  }

  private[sphere] def buildProductDraft(uname: Username, draft: OfferDraft)  = {
    val name = OfferDraft.generateName(draft)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, name).slugified()
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(buildOfferAttributes(uname, draft))
      .build()

    ProductDraftBuilder.of(productTypes.offer, productName, slug, productVariant).build()
  }

  private[sphere] def buildOfferAttributes(uname: Username, offerDraft: OfferDraft) = List(
    Attribute.of("userId", offerDraft.uid.value),
    Attribute.of("userName", uname.value),
    Attribute.of("tags", offerDraft.tags.asJava),
    Attribute.of("longitude", offerDraft.location.lon.value),
    Attribute.of("latitude", offerDraft.location.lat.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(offerDraft.price.value).bigDecimal, "EUR")),
    Attribute.of("images", offerDraft.images.asJava)
  ).asJava

  private[sphere] def getProductById(id: CardId): Future[Option[Product]] =
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)
}

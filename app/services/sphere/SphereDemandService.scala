package services.sphere

import java.util.Locale

import common.domain.{DemandDraft, Username, Version}
import common.exceptions.{MalformedDemand, SphereDeleteFailed, SphereIndexFailed}
import common.helper.ImplicitConversions.OptionConverter
import common.logger.DemandLogger
import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, Versioned}
import io.sphere.sdk.products.commands.{ProductCreateCommand, ProductDeleteCommand}
import io.sphere.sdk.products.queries.{ProductByIdFetch, ProductQuery}
import io.sphere.sdk.products.{ProductDraftBuilder, ProductVariantDraftBuilder, _}
import io.sphere.sdk.utils.MoneyImpl
import model.{CardId, Demand, DemandId}
import play.api.libs.json.Json
import services.UserService

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SphereDemandService(sphereClient:      SphereClient,
                          productTypeDrafts: ProductTypeDrafts,
                          productTypes:      ProductTypes,
                          userService:       UserService) {

  def getDemandById(id: DemandId): Future[Option[Demand]] = {
    getProductById(id) map {
      case Some(product) => Demand.fromProduct(product).toOption
      case None => None
    }
  }

  def getAllDemands: Future[List[Product]] = {
    val productQuery = ProductQuery.of().byProductType(productTypes.demand)

    sphereClient.execute(productQuery) map {
      res => res.getResults.asScala.toList
    }
  }

  def createDemand(draft: DemandDraft): Future[Demand] = {
    def throwAndReportSphereIndexFailed(e: Exception) = {
      DemandLogger.error(s"Demand: ${Json.toJson(draft)} could not be saved in Sphere. " +
        s"Exception: ${e.getMessage}")
      throw new SphereIndexFailed("Error while saving demand in sphere")
    }

    userService.getUserById(draft.uid).flatMap {
      user =>
        val productCreateCommand = ProductCreateCommand.of(buildProductDraft(user.name, draft))

        sphereClient.execute(productCreateCommand) map {
          product => Demand.fromProduct(product).get
        }
    } recover {
      case e: Exception => throwAndReportSphereIndexFailed(e)
    }
  }

  def deleteDemand(id: DemandId, version: Version): Future[Demand] = {
    val product: Versioned[Product] = Versioned.of(id.value, version.value)

    deleteProduct(product) map {
      Demand.fromProduct(_)
        .getOrElse(throw new MalformedDemand("Could not create Demand from Product"))
    }
  }

  def deleteProduct(product: Versioned[Product]) = {
    sphereClient.execute(ProductDeleteCommand.of(product)) recover {
      case e: Exception => throw new SphereDeleteFailed("Demand could not be deleted")
    }
  }

  def deleteAllDemands() = {
    getAllDemands map {
      demandProducts => demandProducts map {
        product => deleteProduct(Versioned.of(product.getId, product.getVersion))
      }
    }
  }

  private[sphere] def buildProductDraft(uname: Username, draft: DemandDraft)  = {
    val name = DemandDraft.generateName(draft)
    val productName = LocalizedStrings.of(Locale.ENGLISH, name)
    val slug = LocalizedStrings.of(Locale.ENGLISH, name).slugified()
    val productVariant = ProductVariantDraftBuilder.of()
      .attributes(buildDemandAttributes(uname, draft))
      .build()

    ProductDraftBuilder.of(productTypes.demand, productName, slug, productVariant).build()
  }

  private[sphere] def buildDemandAttributes(uname: Username, demandDraft: DemandDraft) = List(
    Attribute.of("userId", demandDraft.uid.value),
    Attribute.of("userName", uname.value),
    Attribute.of("mustTags", demandDraft.mustTags.asJava),
    Attribute.of("shouldTags", demandDraft.shouldTags.asJava),
    Attribute.of("longitude", demandDraft.location.lon.value),
    Attribute.of("latitude", demandDraft.location.lat.value),
    Attribute.of("distance", demandDraft.distance.value),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demandDraft.priceMin.value).bigDecimal, "EUR")),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demandDraft.priceMax.value).bigDecimal, "EUR"))
  ).asJava

  private[sphere] def getProductById(id: CardId): Future[Option[Product]] =
    sphereClient.execute(ProductByIdFetch.of(id.value)) map(_.asScala)
}

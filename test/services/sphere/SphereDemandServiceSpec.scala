package services.sphere

import java.util.Locale

import common.domain._
import common.exceptions.{SphereDeleteFailed, SphereIndexFailed}
import common.helper.ConfigLoader
import common.sphere.{MockProductTypes, ProductTypeDrafts, RemoteSphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{Versioned, LocalizedStrings}
import io.sphere.sdk.products
import io.sphere.sdk.products.commands.{ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.{ProductBuilder, ProductCatalogDataBuilder, ProductDataBuilder, ProductVariantBuilder}
import io.sphere.sdk.utils.MoneyImpl
import model.{Demand, DemandId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.test.WithApplication
import services.UserService

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SphereDemandServiceSpec extends Specification with Mockito {

  trait SphereDemandServiceContext extends WithApplication {

    val config = Map("demand.typeName" -> "demand")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val productTypeDrafts = new ProductTypeDrafts(configLoader)
    val productTypes = new MockProductTypes(productTypeDrafts)
    val sphereClientMock = mock[RemoteSphereClient]
    val userService = mock[UserService]
    val service = new SphereDemandService(sphereClientMock, productTypeDrafts, productTypes, userService)

    val username = Username("test")
    val user = User(UserId("abc"), Version(1L), username, Email("test@web.de"))

    val draft = DemandDraft(
      user.id,
      Set("Socken", "Bekleidung"),
      Set("Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Distance(100),
      Price(0.00),
      Price(10.00)
    )

    val demand = Demand(
      DemandId("123"),
      Version(1),
      UserIdAndName(
        draft.uid,
        Username("test")
      ),
      draft.mustTags,
      draft.shouldTags,
      draft.location,
      draft.distance,
      draft.priceMin,
      draft.priceMax
    )

    val productAttributeList = List(
      Attribute.of("userId", demand.user.id.value),
      Attribute.of("userName", demand.user.name.value),
      Attribute.of("mustTags", demand.mustTags.asJava),
      Attribute.of("shouldTags", demand.shouldTags.asJava),
      Attribute.of("longitude", demand.location.lon.value),
      Attribute.of("latitude", demand.location.lat.value),
      Attribute.of("distance", demand.distance.value),
      Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demand.priceMin.value).bigDecimal, "EUR")),
      Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demand.priceMax.value).bigDecimal, "EUR"))
    ).asJava

    val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()

    val productMasterData = ProductCatalogDataBuilder.ofStaged(
      ProductDataBuilder.of(
        LocalizedStrings.of(Locale.ENGLISH, "Suche: Socken Bekleidung"),
        LocalizedStrings.of(Locale.ENGLISH, "suche-socken-bekleidung"),
        productVariant)
        .build())
      .build()

    val demandProduct = ProductBuilder
      .of(productTypes.demand, productMasterData)
      .id(demand.id.value)
      .build()

    val mockProduct = mock[products.Product]
  }

  "SphereDemandService" should {
    "createDemand must throw SphereIndexFailed when SphereClient fails" in new SphereDemandServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns Future.failed(new Exception)

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "createDemand must return demand when SphereClient succeeds" in new SphereDemandServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns Future(demandProduct)

      Await.result(service.createDemand(draft), Duration.Inf) must beEqualTo(demand)
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "createDemand must throw SphereIndexFailed when SphereClient returns invalid product" in new SphereDemandServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns Future(mockProduct)

      Await.result(service.createDemand(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "buildDraft must return valid product drafts" in new SphereDemandServiceContext {
      val productDraft = service.buildProductDraft(username, draft)

      productDraft.getName.get(Locale.ENGLISH).get() must startWith("Suche: Socken Bekleidung")
      productDraft.getSlug.get(Locale.ENGLISH).get() must startWith("suche-socken-bekleidung")
      productDraft.getProductType must beEqualTo(productTypes.demand.toReference)
      productDraft.getMasterVariant.getAttributes must beEqualTo(service.buildDemandAttributes(username, draft))
    }

    "buildDemandAttributes must return valid AttributeList" in new SphereDemandServiceContext {
      val demandAttributes = service.buildDemandAttributes(username, draft).asScala
      demandAttributes must contain(Attribute.of("userId", draft.uid.value))
      demandAttributes must contain(Attribute.of("userName", username.value))
      demandAttributes must contain(Attribute.of("mustTags", draft.mustTags.asJava))
      demandAttributes must contain(Attribute.of("shouldTags", draft.shouldTags.asJava))
      demandAttributes must contain(Attribute.of("longitude", draft.location.lon.value))
      demandAttributes must contain(Attribute.of("latitude", draft.location.lat.value))
      demandAttributes must contain(Attribute.of("distance", draft.distance.value))
      demandAttributes must contain(Attribute.of("priceMin", MoneyImpl.of(BigDecimal(draft.priceMin.value).bigDecimal, "EUR")))
      demandAttributes must contain(Attribute.of("priceMax", MoneyImpl.of(BigDecimal(draft.priceMax.value).bigDecimal, "EUR")))
    }

    "deleteDemand must return SphereDeleteFailed when an exception occurs" in new SphereDemandServiceContext {
      sphereClientMock.execute(any[ProductDeleteCommand]) returns
        Future.failed(new Exception())

      Await.result(service.deleteDemand(demand.id, demand.version), Duration.Inf) must
        throwA[SphereDeleteFailed]
      there was one (sphereClientMock)
        .execute(ProductDeleteCommand.of(Versioned.of(demand.id.value, demand.version.value)))
    }

    "deleteDemand must return demand when sphere succeeds" in new SphereDemandServiceContext {
      sphereClientMock.execute(any[ProductDeleteCommand]) returns Future(demandProduct)

      Await.result(service.deleteDemand(demand.id, demand.version), Duration.Inf) must
        beEqualTo(demand)
      there was one (sphereClientMock)
        .execute(ProductDeleteCommand.of(Versioned.of(demand.id.value, demand.version.value)))
    }
  }
}

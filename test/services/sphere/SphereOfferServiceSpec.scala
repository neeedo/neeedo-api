package services.sphere

import java.util.Locale

import common.domain._
import common.exceptions.{SphereDeleteFailed, SphereIndexFailed}
import common.helper.ConfigLoader
import common.sphere.{MockProductTypes, ProductTypeDrafts, RemoteSphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{Versioned, DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.products
import io.sphere.sdk.products.commands.{ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.{ProductBuilder, ProductCatalogDataBuilder, ProductDataBuilder, ProductVariantBuilder}
import io.sphere.sdk.utils.MoneyImpl
import model.{Offer, OfferId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.test.WithApplication
import services.UserService

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SphereOfferServiceSpec extends Specification with Mockito {

  trait SphereOfferServiceContext extends WithApplication {
    val config = Map("offer.typeName" -> "offer")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val productTypeDrafts = new ProductTypeDrafts(configLoader)
    val productTypes = new MockProductTypes(productTypeDrafts)
    val sphereClientMock = mock[RemoteSphereClient]
    val userService = mock[UserService]
    val service = new SphereOfferService(sphereClientMock, productTypeDrafts, productTypes, userService)

    val username = Username("test")
    val user = User(UserId("abc"), Version(1L), username, Email("test@web.de"))

    val draft = OfferDraft(
      UserId("abc"),
      Set("Socken Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      Set.empty
    )

    val offer = Offer(
      OfferId("123"),
      Version(1),
      UserIdAndName(
        draft.uid,
        username
      ),
      draft.tags,
      draft.location,
      draft.price,
      Set("xyz.jpg")
    )

    val productAttributeList = List(
      Attribute.of("userId", offer.user.id.value),
      Attribute.of("userName", offer.user.name.value),
      Attribute.of("tags", offer.tags.asJava),
      Attribute.of("longitude", offer.location.lon.value),
      Attribute.of("latitude", offer.location.lat.value),
      Attribute.of(
        "price",
        MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal,
          DefaultCurrencyUnits.EUR)),
      Attribute.of("images", offer.images.asJava)
    ).asJava

    val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()

    val productMasterData = ProductCatalogDataBuilder.ofStaged(
      ProductDataBuilder.of(
        LocalizedStrings.of(Locale.ENGLISH, "Biete: Socken Wolle"),
        LocalizedStrings.of(Locale.ENGLISH, "biete-socken-wolle"),
        productVariant)
        .build())
      .build()

    val offerProduct = ProductBuilder
      .of(productTypes.offer, productMasterData)
      .id(offer.id.value)
      .build()

    val mockProduct = mock[products.Product]
  }

  "SphereOfferService" should {
    "createOffer must throw SphereIndexFailed when SphereClient fails" in new SphereOfferServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns
        Future.failed(new Exception)

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "createOffer must return offer when SphereClient succeeds" in new SphereOfferServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns Future(offerProduct)

      Await.result(service.createOffer(draft), Duration.Inf) must beEqualTo(offer)
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "createOffer must throw SphereIndexFailed when SphereClient returns invalid product" in new SphereOfferServiceContext {
      userService.getUserById(any[UserId]) returns Future(user)
      sphereClientMock.execute(any[ProductCreateCommand]) returns
        Future(mockProduct)

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "buildDraft must return valid product drafts" in new SphereOfferServiceContext {
      val productDraft = service.buildProductDraft(username, draft)

      productDraft.getName.get(Locale.ENGLISH).get() must startWith("Biete: Socken Wolle")
      productDraft.getSlug.get(Locale.ENGLISH).get() must startWith("biete-socken-wolle")
      productDraft.getProductType must beEqualTo(productTypes.offer.toReference)
      productDraft.getMasterVariant.getAttributes must beEqualTo(service.buildOfferAttributes(username, draft))
    }

    "buildOfferAttributes must return valid AttributeList" in new SphereOfferServiceContext {
      val offerAttributes = service.buildOfferAttributes(username, draft).asScala
      offerAttributes must contain(Attribute.of("userId", draft.uid.value))
      offerAttributes must contain(Attribute.of("userName", username.value))
      offerAttributes must contain(Attribute.of("tags", draft.tags.asJava))
      offerAttributes must contain(Attribute.of("longitude", draft.location.lon.value))
      offerAttributes must contain(Attribute.of("latitude", draft.location.lat.value))
      offerAttributes must contain(Attribute.of("price", MoneyImpl.of(BigDecimal(draft.price.value).bigDecimal, "EUR")))
      offerAttributes must contain(Attribute.of("images", draft.images.asJava))
    }

    "deleteOffer must return SphereDeleteFailed when an exception occurs" in new SphereOfferServiceContext {
      sphereClientMock.execute(any[ProductDeleteCommand]) returns
        Future.failed(new Exception())

      Await.result(service.deleteOffer(offer.id, offer.version), Duration.Inf) must
        throwA[SphereDeleteFailed]
      there was one (sphereClientMock)
        .execute(ProductDeleteCommand.of(Versioned.of(offer.id.value, offer.version.value)))
    }

    "deleteOffer must return offer when sphere succeeds" in new SphereOfferServiceContext {
      sphereClientMock.execute(any[ProductDeleteCommand]) returns Future(offerProduct)

      Await.result(service.deleteOffer(offer.id, offer.version), Duration.Inf) must
          beEqualTo(offer)
      there was one (sphereClientMock)
        .execute(ProductDeleteCommand.of(Versioned.of(offer.id.value, offer.version.value)))
    }
  }
}

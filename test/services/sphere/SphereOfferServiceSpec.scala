package services.sphere

import java.util.Locale

import common.domain._
import common.exceptions.SphereIndexFailed
import common.helper.ConfigLoader
import common.sphere.{MockProductTypes, ProductTypeDrafts, RemoteSphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantBuilder, ProductBuilder, ProductCatalogDataBuilder, ProductDataBuilder}
import io.sphere.sdk.products.commands.ProductCreateCommand
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import model.{Offer, OfferId}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.test.WithApplication
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class SphereOfferServiceSpec extends Specification with Mockito {

  "SphereOfferService" should {
    "createOffer must throw SphereIndexFailed when SphereClient fails" in new SphereOfferServiceContext {
      sphereClientMock.execute(any[ProductCreateCommand]) returns
        Future.failed(new Exception)

      Await.result(service.createOffer(draft), Duration.Inf) must throwA[SphereIndexFailed]
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "createOffer must return offer when SphereClient succeeds" in new SphereOfferServiceContext {
      sphereClientMock.execute(any[ProductCreateCommand]) returns Future(offerProduct)

      Await.result(service.createOffer(draft), Duration.Inf) must beEqualTo(offer)
      there was one (sphereClientMock).execute(any[ProductCreateCommand])
    }

    "buildDraft must return valid product drafts" in new SphereOfferServiceContext {
      val productDraft = service.buildProductDraft(draft)

      productDraft.getName.get(Locale.ENGLISH).get() must startWith("Biete: Socken Wolle")
      productDraft.getSlug.get(Locale.ENGLISH).get() must startWith("biete-socken-wolle")
      productDraft.getProductType must beEqualTo(productTypes.offer.toReference)
      productDraft.getMasterVariant.getAttributes must beEqualTo(service.buildOfferAttributes(draft))
    }

    "buildOfferAttributes must return valid AttributeList" in new SphereOfferServiceContext {
      val offerAttributes = service.buildOfferAttributes(draft).asScala
      offerAttributes must contain(Attribute.of("userId", draft.uid.value))
      offerAttributes must contain(Attribute.of("tags", draft.tags.mkString(";")))
      offerAttributes must contain(Attribute.of("longitude", draft.location.lon.value))
      offerAttributes must contain(Attribute.of("latitude", draft.location.lat.value))
      offerAttributes must contain(Attribute.of("price", MoneyImpl.of(BigDecimal(draft.price.value).bigDecimal, "EUR")))
    }
  }

  trait SphereOfferServiceContext extends WithApplication {

    val config = Map("offer.typeName" -> "offer")
    val configLoader = new ConfigLoader(Configuration.from(config))
    val productTypeDrafts = new ProductTypeDrafts(configLoader)
    val productTypes = new MockProductTypes(productTypeDrafts)
    val sphereClientMock = mock[RemoteSphereClient]
    val service = new SphereOfferService(sphereClientMock, productTypeDrafts, productTypes)

    val offer = Offer(
      OfferId("123"),
      Version(1),
      UserId("abc"),
      Set("Socken Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00),
      List()
    )

    val draft = OfferDraft(
      UserId("abc"),
      Set("Socken Wolle"),
      Location(Longitude(12.2), Latitude(15.5)),
      Price(50.00)
    )

    val productAttributeList = List(
      Attribute.of("userId", offer.uid.value),
      Attribute.of("tags", offer.tags.mkString(";")),
      Attribute.of("longitude", offer.location.lon.value),
      Attribute.of("latitude", offer.location.lat.value),
      Attribute.of(
        "price",
        MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal,
          DefaultCurrencyUnits.EUR))
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
  }
}

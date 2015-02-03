package model

import java.util.Locale

import common.domain._
import common.sphere.ProductTypeDrafts
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, DefaultCurrencyUnits}
import io.sphere.sdk.products.{ProductBuilder, ProductDataBuilder, ProductCatalogDataBuilder, ProductVariantBuilder}
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.test.WithApplication
import test.TestApplications
import scala.collection.JavaConverters._


class OfferSpec extends Specification {

  val offerJs: JsObject = Json.obj(
    "id" -> "abc123",
    "version" -> 1L,
    "userId" -> "1",
    "tags" -> Set("socken", "bekleidung", "wolle"),
    "location" -> Json.obj(
      "lon" -> 52.468562,
      "lat" -> 13.534212
    ),
    "price" -> 50.0
  )

  val offer = Offer(OfferId("abc123"), Version(1L), UserId("1"), Set("socken", "bekleidung", "wolle"),  Location(Longitude(52.468562), Latitude(13.534212)), Price(50.0))

  val validProductAttributeList = List(
    Attribute.of("userId", offer.uid.value),
    Attribute.of("tags", offer.tags.mkString(";")),
    Attribute.of("longitude", offer.location.lon.value),
    Attribute.of("latitude", offer.location.lat.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val invalidProductAttributeList = List(
    Attribute.of("userId", offer.uid.value),
    Attribute.of("tags", offer.tags),
    Attribute.of("longitude", offer.location.lon.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val validProductVariant = ProductVariantBuilder.of(1).attributes(validProductAttributeList).build()
  val invalidProductVariant = ProductVariantBuilder.of(1).attributes(invalidProductAttributeList).build()

  val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) // Todo provide name generation method

  val validMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, validProductVariant).build()).build()
  val invalidMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, invalidProductVariant).build()).build()

  val validProduct = ProductBuilder.of(productType, validMasterData).id(offer.id.value).build()
  val invalidProduct = ProductBuilder.of(productType, invalidMasterData).id(offer.id.value).build()

  "Offer" should {
    "offer json should be correctly parsed into a offer object" in new WithApplication {
      offerJs.as[Offer] must beEqualTo(offer)
    }

    "offer object should be correctly parsed into a offer option" in new WithApplication {
      Json.toJson(offer) must beEqualTo(offerJs)
    }

    "productToOffer must return valid Offer objects for offer products" in {
      Offer.productToOffer(validProduct) mustEqual Option(offer)
    }

    "productToOffer must return None objects for invalid offer products" in TestApplications.loggingOffApp() {
      Offer.productToOffer(invalidProduct) mustEqual None
    }
  }

  "OfferId" should {
    "be correctly be created from an identifier" in new WithApplication {
      OfferId.pathBinder.bind("key1", "12345abc") mustEqual Right(OfferId("12345abc"))
    }

    "be correctly be transform into an identifier" in new WithApplication {
      OfferId.pathBinder.unbind("key", OfferId("12345abc")) mustEqual("12345abc")
    }
  }
}

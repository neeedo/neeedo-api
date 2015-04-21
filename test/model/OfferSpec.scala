package model

import java.util.Locale

import common.sphere.ProductTypeDrafts
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, DefaultCurrencyUnits}
import io.sphere.sdk.products.{ProductBuilder, ProductDataBuilder, ProductCatalogDataBuilder, ProductVariantBuilder}
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.test.WithApplication
import test.{TestData, TestApplications}
import scala.collection.JavaConverters._


class OfferSpec extends Specification {

  val offerId = TestData.offerId
  val offerJson: JsObject = TestData.offerJson
  val offerJsonWithWhitespaces: JsObject = TestData.offerJsonWithWhitespaces
  val offer = TestData.offer
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) // Todo provide name generation method



  "Offer" should {
    "offer json should be correctly parsed into a offer object" in new WithApplication {
      offerJson.as[Offer] must beEqualTo(offer)
    }

    "offer object should be correctly parsed into a offer option" in new WithApplication {
      Json.toJson(offer) must beEqualTo(offerJson)
    }

    "productToOffer must return valid Offer objects for offer products" in TestApplications.loggingOffApp() {
      val validProductAttributeList = TestData.offerProductAttributeList
      val validProductVariant = ProductVariantBuilder.of(1).attributes(validProductAttributeList).build()
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val validMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, validProductVariant).build()).build()
      val validProduct = ProductBuilder.of(productType, validMasterData).id(offerId.value).build()

      Offer.productToOffer(validProduct) mustEqual Option(offer)
    }

    "productToOffer must return None objects for invalid offer products" in TestApplications.loggingOffApp() {
      val invalidProductAttributeList = List(
        Attribute.of("userId", offer.uid.value),
        Attribute.of("tags", offer.tags),
        Attribute.of("longitude", offer.location.lon.value),
        Attribute.of("price", MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
      ).asJava
      val invalidProductVariant = ProductVariantBuilder.of(1).attributes(invalidProductAttributeList).build()
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val invalidMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, invalidProductVariant).build()).build()
      val invalidProduct = ProductBuilder.of(productType, invalidMasterData).id(offerId.value).build()

      Offer.productToOffer(invalidProduct) mustEqual None
    }

    "trailing whitespaces in taglist must be trimmed" in TestApplications.loggingOffApp() {
      offerJsonWithWhitespaces.as[Offer] must beEqualTo(offer)
    }
  }

  "OfferId" should {
    "be correctly be created from an identifier" in new WithApplication {
      OfferId.pathBinder.bind("key1", offerId.value) mustEqual Right(offerId)
    }

    "be correctly be transform into an identifier" in new WithApplication {
      OfferId.pathBinder.unbind("key", offerId) mustEqual offerId.value
    }
  }
}

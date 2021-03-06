//package model
//
//import java.util.Locale
//
//import common.helper.ConfigLoader
//import common.sphere.ProductTypeDrafts
//import io.sphere.sdk.attributes.Attribute
//import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
//import io.sphere.sdk.products.{ProductBuilder, ProductCatalogDataBuilder, ProductDataBuilder, ProductVariantBuilder}
//import io.sphere.sdk.producttypes.{ProductType, ProductTypeBuilder}
//import io.sphere.sdk.utils.MoneyImpl
//import org.specs2.mutable.Specification
//import play.api.libs.json.Json
//import play.api.test.WithApplication
//import test.{TestApplications, TestData}
//
//import scala.collection.JavaConverters._
//
//
//class DemandSpec extends Specification {
//
//  val demandId = TestData.demandId
//  val demand = TestData.demand
//  val demandJson = TestData.demandJson
//  val demandJsonWithWhiteSpaces = TestData.demandJsonWithWhitespaces
//  val validProductAttributeList = TestData.demandProductAttributeList
//
//  val invalidProductAttributeList = List(
//    Attribute.of("userId", "1"),
//    Attribute.of("mustTags", "socken bekleidung wolle"),
//    Attribute.of("shouldTags", "socken bekleidung wolle"),
//    Attribute.of("latitude", 13.534212),
//    Attribute.of("distance", 30),
//    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(25.0).bigDecimal, DefaultCurrencyUnits.EUR)),
//    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(77.0).bigDecimal, DefaultCurrencyUnits.EUR))
//  ).asJava
//
//  val validProductVariant = ProductVariantBuilder.of(1).attributes(validProductAttributeList).build()
//  val invalidProductVariant = ProductVariantBuilder.of(1).attributes(invalidProductAttributeList).build()
//  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method
//  val validMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, validProductVariant).build()).build()
//  val invalidMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, invalidProductVariant).build()).build()
//
//  "Demand" should {
//    "demand json should be correctly parsed into a demand object" in new WithApplication {
//      demandJson.as[Demand] must beEqualTo(demand)
//    }
//
//    "demand object should be correctly parsed into a demand json" in new WithApplication {
//      Json.toJson(demand) must beEqualTo(demandJson)
//    }
//
//    "fromProduct must return valid Demand objects for demand products" in TestApplications.loggingOffApp() {
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val validProduct = ProductBuilder.of(productType, validMasterData).id(demandId.value).build()
//      Demand.fromProduct(validProduct) mustEqual Option(demand)
//    }
//
//    "fromProduct must return None objects for invalid demand products" in TestApplications.loggingOffApp() {
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val invalidProduct = ProductBuilder.of(productType, invalidMasterData).id(demandId.value).build()
//      Demand.fromProduct(invalidProduct) mustEqual None
//    }
//
//    "trailing whitespaces in taglists must be trimmed" in TestApplications.loggingOffApp() {
//      demandJsonWithWhiteSpaces.as[Demand] must beEqualTo(demand)
//    }
//  }
//
//  "DemandId" should {
//    "be correctly be created from an identifier" in new WithApplication {
//      DemandId.pathBindable.bind("key1", demandId.value) mustEqual Right(demandId)
//    }
//
//    "be correctly be transform into an identifier" in new WithApplication {
//      DemandId.pathBindable.unbind("key", demandId) mustEqual demandId.value
//    }
//  }
//
//}

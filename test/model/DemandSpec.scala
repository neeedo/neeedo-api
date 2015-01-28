package model

import java.util.Locale
import common.domain.Price
import common.domain._
import common.sphere.ProductTypeDrafts
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantBuilder, ProductCatalogDataBuilder, ProductBuilder}
import io.sphere.sdk.products.ProductDataBuilder
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import org.specs2.mutable.Specification
import play.api.libs.json.{JsObject, Json}
import play.api.test.WithApplication
import test.TestApplications
import scala.collection.JavaConverters._


class DemandSpec extends Specification {

  val demandId = DemandId("foo-id")
  val demandVersion = Version(1L)

  val demandJs: JsObject = Json.obj(
    "id" -> demandId.value,
    "version" -> 1L,
    "userId" -> "1",
    "tags" -> "socken bekleidung wolle",
    "location" -> Json.obj(
      "lon" -> 52.468562,
      "lat" -> 13.534212
    ),
    "distance" -> 30,
    "price" -> Json.obj(
      "min" -> 25.0,
      "max" -> 77.0
    )
  )

  val validProductAttributeList = List(
    Attribute.of("userId", "1"),
    Attribute.of("tags", "socken bekleidung wolle"),
    Attribute.of("longitude", 52.468562),
    Attribute.of("latitude", 13.534212),
    Attribute.of("distance", 30),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(25.0).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(77.0).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val invalidProductAttributeList = List(
    Attribute.of("userId", "1"),
    Attribute.of("tags", "socken bekleidung wolle"),
    Attribute.of("latitude", 13.534212),
    Attribute.of("distance", 30),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(25.0).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(77.0).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val demand = Demand(demandId, demandVersion, UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
  
  val validProductVariant = ProductVariantBuilder.of(1).attributes(validProductAttributeList).build()
  val invalidProductVariant = ProductVariantBuilder.of(1).attributes(invalidProductAttributeList).build()

  val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle")


  val validMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, validProductVariant).build()).build()
  val invalidMasterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, invalidProductVariant).build()).build()

  val validProduct = ProductBuilder.of(productType, validMasterData).id(demandId.value).build()
  val invalidProduct = ProductBuilder.of(productType, invalidMasterData).id(demandId.value).build()


  "Demand" should {
    "demand json should be correctly parsed into a demand object" in new WithApplication {
      demandJs.as[Demand] must beEqualTo(demand)
    }

    "demand object should be correctly parsed into a demand json" in new WithApplication {
      Json.toJson(demand) must beEqualTo(demandJs)
    }

    "productToDemand must return valid Demand objects for demand products" in {
      Demand.productToDemand(validProduct) mustEqual Option(demand)
    }

    "productToDemand must return None objects for invalid demand products" in TestApplications.loggingOffApp {
      Demand.productToDemand(invalidProduct) mustEqual None
    }

  }

  "DemandId" should {
    "be correctly be created from an identifier" in new WithApplication {
      DemandId.pathBinder.bind("key1", "12345abc") mustEqual Right(DemandId("12345abc"))
    }

    "be correctly be transform into an identifier" in new WithApplication {
      DemandId.pathBinder.unbind("key", DemandId("12345abc")) mustEqual("12345abc")
    }
  }

}

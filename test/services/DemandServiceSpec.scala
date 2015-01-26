package services

import java.util.Locale
import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.products.ProductVariantBuilder
import io.sphere.sdk.products.ProductDataBuilder
import io.sphere.sdk.products.ProductCatalogDataBuilder
import io.sphere.sdk.products.ProductBuilder
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication
import scala.collection.JavaConverters._
import scala.concurrent.Future

class DemandServiceSpec extends Specification with Mockito {

  "Demand service" should {
    val attributeList = List(
      Attribute.of("userId", "1"),
      Attribute.of("tags", "socken bekleidung wolle"),
      Attribute.of("longitude", 52.468562),
      Attribute.of("latitude", 13.534212),
      Attribute.of("distance", 30),
      Attribute.of("priceMin", MoneyImpl.of(BigDecimal(25.0).bigDecimal, DefaultCurrencyUnits.EUR)),
      Attribute.of("priceMax", MoneyImpl.of(BigDecimal(77.0).bigDecimal, DefaultCurrencyUnits.EUR))
    ).asJava

    val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()
    val emptyProductVariant = ProductVariantBuilder.of(1).attributes(attributeList).build()
    val name = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle")
    val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(name, name, emptyProductVariant).build()).build()
    val product = ProductBuilder.of(productType, masterData).id("foo-id").build()
    val demand = Demand(DemandId("foo-id"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

    "productToDemand must return valid Demand objects" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val demandService = new DemandService(es, sphere, productTypes)

      demandService.productToDemand(product) mustEqual demand
    }

    "getProductById must call Sphereclient execute with fetchcommand" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val demandService = new DemandService(es, sphere, productTypes)

      demandService.getProductById(DemandId("1"))
      there was one (sphere).execute(ProductFetchById.of("1"))
    }

    "createDemand must return None if writing to sphere fails" in new WithApplication {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
      productTypes.demand returns ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()

      val demandService = new DemandService(es, sphere, productTypes)

      val demandDraft = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
    }

    "createDemand must return None if writing to es fails and deleteDemand should be called once with correct parameters" in new WithApplication {
      val demandDraft = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      es.indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand)) returns Future.failed(new RuntimeException("test exception"))

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
    }
  }
}

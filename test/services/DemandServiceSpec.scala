package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantBuilder, ProductVariantDraftBuilder, ProductCatalogDataBuilder, ProductDataBuilder, ProductBuilder}
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import java.util.{Optional, Locale}
import java.util.concurrent.CompletionException
import model.{DemandId, Demand}
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import scala.collection.JavaConverters._
import scala.concurrent.Future
import test.TestApplications

class DemandServiceSpec extends Specification with Mockito {

  val productAttributeList = List(
    Attribute.of("userId", "1"),
    Attribute.of("tags", "socken bekleidung wolle"),
    Attribute.of("longitude", 52.468562),
    Attribute.of("latitude", 13.534212),
    Attribute.of("distance", 30),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(25.0).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(77.0).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val demandId = DemandId("foo-id")
  val demandVersion = Version(1L)

  val demand = Demand(demandId, demandVersion, UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))
  val demandDraft = DemandDraft(UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

  val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()
  val productVariantDraft = ProductVariantDraftBuilder.of().attributes(productAttributeList).build()

  val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle")

  val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
  val product = ProductBuilder.of(productType, masterData).id(demandId.value).build()

  "Demand service" should {

    "getProductById must call Sphereclient execute with fetchcommand" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val demandService = new DemandService(es, sphere, productTypes)

      demandService.getProductById(DemandId("1"))
      there was one (sphere).execute(ProductFetchById.of("1"))
    }

    "createDemand must return None if writing to sphere fails" in TestApplications.loggingOffApp {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
      productTypes.demand returns ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()

      val demandService = new DemandService(es, sphere, productTypes)

      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
      there was one (sphere).execute(any[ProductCreateCommand])
    }

    "createDemand must return None if writing to es fails and call sphere execute twice" in TestApplications.loggingOffApp {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      es.indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand)) returns Future.failed(new RuntimeException("test exception"))

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand))
    }

    "createDemand must return Future[Option[Demand]] if parameters are valid" in TestApplications.loggingOffApp {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      es.indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand)) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.createDemand(demandDraft) must beEqualTo(Option(demand)).await
      there was one (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand))
    }

    "writeDemandToEs must return DemandSaveFailed when IndexResponse is not created" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
      es.indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand)) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.writeDemandToEs(demand) must beEqualTo(DemandSaveFailed).await
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand))
    }

    "deleteDemand must return Option.empty[Product] when sphere execute throws CompletionException" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.failed(new CompletionException(new Exception()))

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.deleteDemand(demandId, demandVersion) must beEqualTo(Option.empty[Product]).await
      there was one (sphere).execute(any)
    }

    "getDemand by Id must return valid Demand if sphere returns valid Product" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(ProductFetchById.of(demandId.value)) returns Future.successful(Optional.of(product))

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.getDemandById(demandId) must beEqualTo(Option(demand)).await
      there was one (sphere).execute(any)
    }

    "getDemand by Id must return empty Option if sphere returns Option empty" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(ProductFetchById.of(demandId.value)) returns Future.successful(Optional.empty())

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.getDemandById(demandId) must beEqualTo(Option.empty[Demand]).await
      there was one (sphere).execute(any)
    }

    "updateDemand must return demand with valid parameters and call sphere twice" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      es.indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand)) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.updateDemand(demandId, demandVersion, demandDraft) must beEqualTo(Option(demand)).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), Json.toJson(demand))
    }
  }
}

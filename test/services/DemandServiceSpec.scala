package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products.{ProductVariantBuilder, ProductVariantDraftBuilder, ProductCatalogDataBuilder, ProductDataBuilder, ProductBuilder}
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import java.util.{Optional, Locale}
import java.util.concurrent.CompletionException
import model.{DemandId, Demand}
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.{TestData, TestApplications}
import scala.concurrent.Future

class DemandServiceSpec extends Specification with Mockito {

  val demandId = TestData.demandId
  val demandVersion = TestData.version
  val demand = TestData.demand
  val demandJson = TestData.demandJson
  val demandDraft = TestData.demandDraft
  val productAttributeList = TestData.demandProductAttributeList

  val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()
  val productVariantDraft = ProductVariantDraftBuilder.of().attributes(productAttributeList).build()

  val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method

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

    "createDemand must return None if writing to sphere fails" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
      productTypes.demand returns ProductTypeBuilder.of("id", ProductTypeDrafts.demand).build()

      val demandService = new DemandService(es, sphere, productTypes)

      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
      there was one (sphere).execute(any[ProductCreateCommand])
    }

    "createDemand must return None if writing to es fails and call sphere execute twice" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      es.indexDocument(IndexName("demands"), TypeName("demands"), demandJson) returns Future.failed(new RuntimeException("test exception"))

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), demandJson)
    }

    "createDemand must return Future[Option[Demand]] if parameters are valid" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.demand returns ProductTypeBuilder.of("demand", ProductTypeDrafts.demand).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      es.indexDocument(IndexName("demands"), TypeName("demands"), demandJson) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.createDemand(demandDraft) must beEqualTo(Option(demand)).await
      there was one (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), demandJson)
    }

    "writeDemandToEs must return DemandSaveFailed when IndexResponse is not created" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
      es.indexDocument(IndexName("demands"), TypeName("demands"), demandJson) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.writeDemandToEs(demand) must beEqualTo(DemandSaveFailed).await
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), demandJson)
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
      es.indexDocument(IndexName("demands"), TypeName("demands"), demandJson) returns Future.successful(indexResponse)

      val demandService = new DemandService(es, sphere, productTypes)
      demandService.updateDemand(demandId, demandVersion, demandDraft) must beEqualTo(Option(demand)).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(IndexName("demands"), TypeName("demands"), demandJson)
    }
  }
}

//package services
//
//import java.util.concurrent.CompletionException
//import java.util.{Locale, Optional}
//
//import common.domain._
//import common.elasticsearch.ElasticsearchClient
//import common.helper.ConfigLoader
//import common.sphere.{ProductTypeDrafts, ProductTypes, SphereClient}
//import io.sphere.sdk.models.LocalizedStrings
//import io.sphere.sdk.products._
//import io.sphere.sdk.products.commands.{ProductCreateCommand, ProductDeleteCommand}
//import io.sphere.sdk.products.queries.ProductByIdFetch
//import io.sphere.sdk.producttypes.{ProductType, ProductTypeBuilder}
//import model.Demand
//import org.elasticsearch.action.index.IndexResponse
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//import play.api.libs.json.JsValue
//import test.{TestApplications, TestData}
//
//import scala.concurrent.Future
//
//class DemandServiceSpec extends Specification with Mockito {
//
//  val demandId = TestData.demandId
//  val demandVersion = TestData.version
//  val demand = TestData.demand
//  val demandJson = TestData.demandJson
//  val demandJsonWithCompletionTags = TestData.demandJsonWithCompletionTags
//  val demandDraft = TestData.demandDraft
//  val productAttributeList = TestData.demandProductAttributeList
//
//  val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()
//  val productVariantDraft = ProductVariantDraftBuilder.of().attributes(productAttributeList).build()
//
//  val demandIndex = IndexName("demand")
//  val demandType = demandIndex.toTypeName
//
//  "Demand service" should {
//
//    "getProductById must call Sphereclient execute with fetchcommand" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      sphere.execute(ProductByIdFetch.of(demandId.value)) returns Future(Optional.empty())
//
//      val productTypes = mock[ProductTypes]
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//
//      demandService.getProductById(demandId) must be (Option.empty[Product]).await
//      there was one (sphere).execute(ProductByIdFetch.of(demandId.value))
//    }
//
//    "createDemand must return None if writing to sphere fails" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
//      productTypes.demand returns ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//
//      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
//      there was one (sphere).execute(any[ProductCreateCommand])
//    }
//
//    "createDemand must return None if writing to es fails and call sphere execute twice" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method
//
//      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
//      val product = ProductBuilder.of(productType, masterData).id(demandId.value).build()
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      sphere.execute(any[ProductDeleteCommand]) returns Future.successful(product)
//      productTypes.demand returns ProductTypeBuilder.of("demand", productTypeDrafts.demand).build()
//      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
//      val anyType: TypeName = any[String].asInstanceOf[TypeName]
//      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.failed(new RuntimeException("test exception"))
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.createDemand(demandDraft) must be (Option.empty[Demand]).await
//      there was two (sphere).execute(any)
//      there was one (es).indexDocument(demandId.value, demandIndex, demandType, demandJsonWithCompletionTags)
//    }
//
//    "createDemand must return Future[Option[Demand]] if parameters are valid" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method
//
//      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
//      val product = ProductBuilder.of(productType, masterData).id(demandId.value).build()
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      productTypes.demand returns ProductTypeBuilder.of("demand", productTypeDrafts.demand).build()
//      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
//      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
//      val anyType: TypeName = any[String].asInstanceOf[TypeName]
//      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.createDemand(demandDraft) must beEqualTo(Option(demand)).await
//      there was one (sphere).execute(any)
//      there was one (es).indexDocument(demandId.value, demandIndex, demandType, demandJsonWithCompletionTags)
//    }
//
//    "writeDemandToEs must return DemandSaveFailed when IndexResponse is not created" in
//      TestApplications.configOffApp(Map("demand.typeName" -> demandIndex.value)) {
//
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
//      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
//      val anyType: TypeName = any[String].asInstanceOf[TypeName]
//      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.writeDemandToEs(demand) must beEqualTo(DemandSaveFailed).await
//      there was one (es).indexDocument(demandId.value, demandIndex, demandType, demandJsonWithCompletionTags)
//    }
//
//    "deleteDemand must return Option.empty[Product] when sphere execute throws CompletionException" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductDeleteCommand]) returns Future.failed(new CompletionException(new Exception()))
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.deleteDemand(demandId, demandVersion) must beEqualTo(Option.empty[Product]).await
//      there was one (sphere).execute(any)
//    }
//
//    "getDemand by Id must return valid Demand if sphere returns valid Product" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method
//
//      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
//      val product = ProductBuilder.of(productType, masterData).id(demandId.value).build()
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(ProductByIdFetch.of(demandId.value)) returns Future.successful(Optional.of(product))
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.getDemandById(demandId) must beEqualTo(Option(demand)).await
//      there was one (sphere).execute(any)
//    }
//
//    "getDemand by Id must return empty Option if sphere returns Option empty" in TestApplications.loggingOffApp() {
//      val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(ProductByIdFetch.of(demandId.value)) returns Future.successful(Optional.empty())
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.getDemandById(demandId) must beEqualTo(Option.empty[Demand]).await
//      there was one (sphere).execute(any)
//    }
//
//    "updateDemand must return demand with valid parameters and call sphere twice" in
//      TestApplications.configOffApp(Map("offer.typeName" -> demandIndex.value)) {
//
//        val config = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(config)
//
//      val productType: ProductType = ProductTypeBuilder.of("id", productTypeDrafts.demand).build()
//      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, "socken bekleidung wolle") // Todo provide name method
//
//      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
//      val product = ProductBuilder.of(productType, masterData).id(demandId.value).build()
//
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      productTypes.demand returns ProductTypeBuilder.of("demand", productTypeDrafts.demand).build()
//      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
//      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
//      val anyType: TypeName = any[String].asInstanceOf[TypeName]
//      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)
//      es.deleteDocument(demandId.value, demandIndex, demandType) returns Future.successful(true)
//
//      val demandService = new DemandService(es, sphere, productTypes, productTypeDrafts, config)
//      demandService.updateDemand(demandId, demandVersion, demandDraft) must beEqualTo(Option(demand)).await
//      there was two (sphere).execute(any)
//      there was one (es).indexDocument(demandId.value, demandIndex, demandType, demandJsonWithCompletionTags)
//      there was one (es).deleteDocument(demandId.value, demandIndex, demandType)
//    }
//  }
//}

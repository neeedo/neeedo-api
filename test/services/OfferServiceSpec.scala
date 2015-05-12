package services

import java.util.concurrent.TimeUnit

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.exceptions.{SphereIndexFailed, ElasticSearchIndexFailed}
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products._
import io.sphere.sdk.products.commands.{ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import java.util.{Optional, Locale}
import play.api.libs.json.JsValue

import scala.concurrent.{Await, Future}
import model.Offer
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.{TestApplications, TestData}
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

class OfferServiceSpec extends Specification with Mockito {

  val offerId = TestData.offerId
  val offerVersion = TestData.version
  val offer = TestData.offer
  val offerJson = TestData.offerJson
  val offerJsonWithCompletionTags = TestData.offerJsonWithCompletionTags
  val offerDraft = TestData.offerDraft
  val productAttributeList = TestData.offerProductAttributeList

  val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()
  val productVariantDraft = ProductVariantDraftBuilder.of().attributes(productAttributeList).build()

  val offerIndex = IndexName("offer")
  val offerType = offerIndex.toTypeName

  "getProductById" should {
    "call Sphereclient execute with fetchcommand" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      sphere.execute(ProductByIdFetch.of(anyString)) returns Future(Optional.empty())
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]
      val service = new OfferService(es, sphere, productTypes, esCompletionService)

      service.getProductById(offerId) must be (Option.empty[Product]).await
      there was one (sphere).execute(ProductByIdFetch.of(offerId.value))
    }
  }

  "createOffer" should {
    "return exception if writing to sphere fails" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("writing to es failed"))
      productTypes.offer returns ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()

      val service = new OfferService(es, sphere, productTypes, esCompletionService)

      Await.result(service.createOffer(offerDraft), Duration(3, SECONDS)) must throwA(new SphereIndexFailed("Error while saving offer in sphere"))
      there was one (sphere).execute(any[ProductCreateCommand])
    }

    "return exception if writing to es fails and call sphere execute twice" in TestApplications.loggingOffApp() {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      sphere.execute(any[ProductDeleteCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()

      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
      val anyType: TypeName = any[String].asInstanceOf[TypeName]
      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.failed(new RuntimeException("test exception"))

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      Await.result(service.createOffer(offerDraft), Duration(3, SECONDS)) must throwA(new ElasticSearchIndexFailed("Error while saving offer in elasticsearch"))
      there was two (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJsonWithCompletionTags)
    }

    "return Future[Offer] if parameters are valid" in TestApplications.loggingOffApp() {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
      val anyType: TypeName = any[String].asInstanceOf[TypeName]
      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      service.createOffer(offerDraft) must beEqualTo(offer).await
      there was one (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJsonWithCompletionTags)
    }
  }

  "writeOfferToEs" should {
    "return when IndexResponse is not created" in
      TestApplications.configOffApp(Map("offer.typeName" -> offerIndex.value)) {
        val es = mock[ElasticsearchClient]
        val sphere = mock[SphereClient]
        val productTypes = mock[ProductTypes]
        val esCompletionService = mock[EsCompletionService]

        val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
        val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
        val anyType: TypeName = any[String].asInstanceOf[TypeName]
        es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)

        val service = new OfferService(es, sphere, productTypes, esCompletionService)

        Await.result(service.writeOfferToEs(offer), Duration(3, SECONDS)) must throwA(new ElasticSearchIndexFailed("Error while saving offer in elasticsearch"))

        there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJsonWithCompletionTags)
    }
  }

  "deleteOffer" should {
    "return failed future when sphere execute throws CompletionException" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(any[ProductDeleteCommand]) returns Future.failed(new Exception("bla"))

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      Await.result(service.deleteOffer(offerId, offerVersion), Duration(3, SECONDS)) must throwA(new Exception("bla"))
      there was one (sphere).execute(any)
    }
  }

  "getOfferById" should {
    "return valid Offer if sphere returns valid Product" in TestApplications.loggingOffApp() {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(ProductByIdFetch.of(offerId.value)) returns Future.successful(Optional.of(product))

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      service.getOfferById(offerId) must beEqualTo(Option(offer)).await
      there was one (sphere).execute(any)
    }

    "return empty Option if sphere returns Option empty" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(ProductByIdFetch.of(offerId.value)) returns Future.successful(Optional.empty())

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      service.getOfferById(offerId) must beEqualTo(Option.empty[Offer]).await
      there was one (sphere).execute(any)
    }
  }

  "updateOffer" should {
    "return Offer with valid parameters and call sphere twice" in
      TestApplications.configOffApp(Map("offer.typeName" -> offerIndex.value)) {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val esCompletionService = mock[EsCompletionService]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      val anyIndex: IndexName = any[String].asInstanceOf[IndexName]
      val anyType: TypeName = any[String].asInstanceOf[TypeName]
      es.indexDocument(anyString, anyIndex, anyType, any[JsValue]) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes, esCompletionService)
      service.updateOffer(offerId, offerVersion, offerDraft) must beEqualTo(offer).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJsonWithCompletionTags)
    }
  }

}

package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, ProductTypeDrafts, SphereClient}
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products._
import io.sphere.sdk.products.commands.{ProductDeleteCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductByIdFetch
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import java.util.{Optional, Locale}
import java.util.concurrent.CompletionException
import model.Offer
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import test.{TestData, TestApplications}
import scala.concurrent.Future

class OfferServiceSpec extends Specification with Mockito {

  val offerId = TestData.offerId
  val offerVersion = TestData.version
  val offer = TestData.offer
  val offerJson = TestData.offerJson
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
      sphere.execute(ProductByIdFetch.of(offerId.value)) returns Future(Optional.empty())
      val productTypes = mock[ProductTypes]
      val service = new OfferService(es, sphere, productTypes)

      service.getProductById(offerId) must be (Option.empty[Product]).await
      there was one (sphere).execute(ProductByIdFetch.of(offerId.value))
    }
  }

  "createOffer" should {
    "return None if writing to sphere fails" in TestApplications.loggingOffApp() {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
      productTypes.offer returns ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()

      val service = new OfferService(es, sphere, productTypes)

      service.createOffer(offerDraft) must be (Option.empty[Offer]).await
      there was one (sphere).execute(any[ProductCreateCommand])
    }

    "return None if writing to es fails and call sphere execute twice" in TestApplications.loggingOffApp() {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      sphere.execute(any[ProductDeleteCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
      es.indexDocument(offerId.value, offerIndex, offerType, offerJson) returns Future.failed(new RuntimeException("test exception"))

      val service = new OfferService(es, sphere, productTypes)
      service.createOffer(offerDraft) must be (Option.empty[Offer]).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJson)
    }

    "return Future[Option[Offer]] if parameters are valid" in TestApplications.loggingOffApp() {
      val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
      val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags.mkString(";")) //Todo proveide name method

      val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
      val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      es.indexDocument(offerId.value, offerIndex, offerType, offerJson) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes)
      service.createOffer(offerDraft) must beEqualTo(Option(offer)).await
      there was one (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJson)
    }
  }

  "writeOfferToEs" should {
    "return OfferSaveFailed when IndexResponse is not created" in
      TestApplications.configOffApp(Map("offer.typeName" -> offerIndex.value)) {

        val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
      es.indexDocument(offerId.value, offerIndex, offerType, offerJson) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes)
      service.writeOfferToEs(offer) must beEqualTo(OfferSaveFailed).await
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJson)
    }
  }

  "deleteOffer" should {
    "return Option.empty[Product] when sphere execute throws CompletionException" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductDeleteCommand]) returns Future.failed(new CompletionException(new Exception()))

      val service = new OfferService(es, sphere, productTypes)
      service.deleteOffer(offerId, offerVersion) must beEqualTo(Option.empty[Product]).await
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

      sphere.execute(ProductByIdFetch.of(offerId.value)) returns Future.successful(Optional.of(product))

      val service = new OfferService(es, sphere, productTypes)
      service.getOfferById(offerId) must beEqualTo(Option(offer)).await
      there was one (sphere).execute(any)
    }

    "return empty Option if sphere returns Option empty" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(ProductByIdFetch.of(offerId.value)) returns Future.successful(Optional.empty())

      val service = new OfferService(es, sphere, productTypes)
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

      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
      es.indexDocument(offerId.value, offerIndex, offerType, offerJson) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes)
      service.updateOffer(offerId, offerVersion, offerDraft) must beEqualTo(Option(offer)).await
      there was two (sphere).execute(any)
      there was one (es).indexDocument(offerId.value, offerIndex, offerType, offerJson)
    }
  }

}

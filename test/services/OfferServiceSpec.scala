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
import model.{OfferId, Offer}
import org.elasticsearch.action.index.IndexResponse
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.{FakeApplication, WithApplication}
import scala.collection.JavaConverters._
import scala.concurrent.Future

class OfferServiceSpec extends Specification with Mockito {

  // Todo outsource
  val WithQuietApplication = new WithApplication(FakeApplication(additionalConfiguration = Map("logger.application" -> "OFF"))){}

  val offer = Offer(OfferId("abc123"), Version(1L), UserId("1"), "socken bekleidung wolle und mehr",  Location(Longitude(52.468562), Latitude(13.534212)), Price(50.0))
  val offerDraft = OfferDraft(offer.uid, offer.tags, offer.location, offer.price)

  val productAttributeList = List(
    Attribute.of("userId", offer.uid.value),
    Attribute.of("tags", offer.tags),
    Attribute.of("longitude", offer.location.lon.value),
    Attribute.of("latitude", offer.location.lat.value),
    Attribute.of("price", MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  val productVariant = ProductVariantBuilder.of(1).attributes(productAttributeList).build()
  val productVariantDraft = ProductVariantDraftBuilder.of().attributes(productAttributeList).build()

//  val productType: ProductType = ProductTypeBuilder.of("id2", ProductTypeDrafts.offer).build()
  val productNameAndSlug = LocalizedStrings.of(Locale.ENGLISH, offer.tags)

  val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(productNameAndSlug, productNameAndSlug, productVariant).build()).build()
//  val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

//  "productToOffer" should {
//    "return valid Offer objects" in {
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//      val service = new OfferService(es, sphere, productTypes)
//
//      service.productToOffer(product) mustEqual offer
//    }
//  }

  "getProductById" should {
    "call Sphereclient execute with fetchcommand" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]
      val service = new OfferService(es, sphere, productTypes)

      service.getProductById(OfferId("1"))
      there was one (sphere).execute(ProductFetchById.of("1"))
    }
  }

  "createOffer" should {
    "return None if writing to sphere fails" in WithQuietApplication {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
      productTypes.offer returns ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()

      val service = new OfferService(es, sphere, productTypes)

      service.createOffer(offerDraft) must be (Option.empty[Offer]).await
      there was one (sphere).execute(any[ProductCreateCommand])
    }

//    "return None if writing to es fails and call sphere execute twice" in WithQuietApplication {
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.successful(product)
//      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
//      es.indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer)) returns Future.failed(new RuntimeException("test exception"))
//
//      val service = new OfferService(es, sphere, productTypes)
//      service.createOffer(offerDraft) must be (Option.empty[Offer]).await
//      there was two (sphere).execute(any)
//      there was one (es).indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer))
//    }

//    "return Future[Option[Offer]] if parameters are valid" in WithQuietApplication {
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
//      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
//      es.indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer)) returns Future.successful(indexResponse)
//
//      val service = new OfferService(es, sphere, productTypes)
//      service.createOffer(offerDraft) must beEqualTo(Option(offer)).await
//      there was one (sphere).execute(any)
//      there was one (es).indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer))
//    }
  }

  "writeOfferToEs" should {
    "return OfferSaveFailed when IndexResponse is not created" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      val indexResponse: IndexResponse = new IndexResponse("","","",1L,false)
      es.indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer)) returns Future.successful(indexResponse)

      val service = new OfferService(es, sphere, productTypes)
      service.writeOfferToEs(offer) must beEqualTo(OfferSaveFailed).await
      there was one (es).indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer))
    }
  }

  "deleteOffer" should {
    "return Option.empty[Product] when sphere execute throws CompletionException" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(any[ProductDeleteByIdCommand]) returns Future.failed(new CompletionException(new Exception()))

      val service = new OfferService(es, sphere, productTypes)
      service.deleteOffer(offer.id, offer.version) must beEqualTo(Option.empty[Product]).await
      there was one (sphere).execute(any)
    }
  }

  "getOfferById" should {
//    "return valid Offer if sphere returns valid Product" in {
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(ProductFetchById.of(offer.id.value)) returns Future.successful(Optional.of(product))
//
//      val service = new OfferService(es, sphere, productTypes)
//      service.getOfferById(offer.id) must beEqualTo(Option(offer)).await
//      there was one (sphere).execute(any)
//    }

    "return empty Option if sphere returns Option empty" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val productTypes = mock[ProductTypes]

      sphere.execute(ProductFetchById.of(offer.id.value)) returns Future.successful(Optional.empty())

      val service = new OfferService(es, sphere, productTypes)
      service.getOfferById(offer.id) must beEqualTo(Option.empty[Offer]).await
      there was one (sphere).execute(any)
    }
  }

//  "updateOffer" should {
//    "return Offer with valid parameters and call sphere twice" in {
//      val es = mock[ElasticsearchClient]
//      val sphere = mock[SphereClient]
//      val productTypes = mock[ProductTypes]
//
//      sphere.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
//      val indexResponse: IndexResponse = new IndexResponse("","","",1L,true)
//      es.indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer)) returns Future.successful(indexResponse)
//
//      val service = new OfferService(es, sphere, productTypes)
//      service.updateOffer(offer.id, offer.version, offerDraft) must beEqualTo(Option(offer)).await
//      there was two (sphere).execute(any)
//      there was one (es).indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer))
//    }
//  }

}

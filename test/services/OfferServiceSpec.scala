package services

import java.util.Locale

import common.domain._
import io.sphere.sdk.products.commands.{ProductDeleteByIdCommand, ProductCreateCommand}
import io.sphere.sdk.products.queries.ProductFetchById
import model._
import common.elasticsearch.ElasticsearchClient
import common.sphere.{ProductTypes, SphereClient, ProductTypeDrafts}
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.{LocalizedStrings, DefaultCurrencyUnits}
import io.sphere.sdk.products.{ProductBuilder, ProductDataBuilder, ProductCatalogDataBuilder, ProductVariantBuilder}
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.utils.MoneyImpl
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.json.Json
import play.api.test.WithApplication
import scala.collection.JavaConverters._
import scala.concurrent.Future

class OfferServiceSpec extends Specification with Mockito {
//  val offer = Offer(OfferId("123456789abc"), Version(1L), UserId("1"), "socken bekleidung wolle",  Location(Longitude(52.468562), Latitude(13.534212)), Price(50.0))
//  val offerDraft = OfferDraft(offer.uid, offer.tags, offer.location, offer.price)
//  val attributeList = List(
//    Attribute.of("userId", offer.uid.value),
//    Attribute.of("tags", offer.tags),
//    Attribute.of("longitude", offer.location.lon.value),
//    Attribute.of("latitude", offer.location.lat.value),
//    Attribute.of("price", MoneyImpl.of(BigDecimal(offer.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
//  ).asJava
//
//  val esClient = mock[ElasticsearchClient]
//  val sphereClient = mock[SphereClient]
//  val productTypes = mock[ProductTypes]
//  val offerService = new OfferService(esClient, sphereClient, productTypes)
//  val productType: ProductType = ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()
//  val emptyProductVariant = ProductVariantBuilder.of(1).attributes(attributeList).build()
//  val name = LocalizedStrings.of(Locale.ENGLISH, offer.tags)
//  val masterData = ProductCatalogDataBuilder.ofStaged(ProductDataBuilder.of(name, name, emptyProductVariant).build()).build()
//  val product = ProductBuilder.of(productType, masterData).id(offer.id.value).build()

//  "productToOffer" should {
//    "return a valid Offer Object" in {
//      offerService.productToOffer(product) mustEqual offer
//    }
//  }
//
//  "getProductById" should {
//    "call SphereClient execute with fetch command" in {
//      offerService.getProductById(OfferId("1"))
//      there was one (sphereClient).execute(ProductFetchById.of("1"))
//    }
//  }
//
//  "createOffer" should {
//
//    "return None if writing to sphere fails" in new WithApplication {
//      sphereClient.execute(any[ProductCreateCommand]) returns Future.failed(new RuntimeException("test exception"))
//      productTypes.offer returns ProductTypeBuilder.of("id", ProductTypeDrafts.offer).build()
//
//      offerService.createOffer(offerDraft) must be (Option.empty[Offer]).await
//    }
//
//    "return None if writing to es fails and " +
//      "deleteOffer should be called once with correct parameters" in new WithApplication() {
//      sphereClient.execute(any[ProductCreateCommand]) returns Future.successful(product)
//      sphereClient.execute(any[ProductDeleteByIdCommand]) returns Future.successful(product)
//      // should we get offer.typeName from ConfigLoader here? Same for Indexnames
//      productTypes.offer returns ProductTypeBuilder.of("offer", ProductTypeDrafts.offer).build()
//      esClient.indexDocument(IndexName("offers"), TypeName("offers"), Json.toJson(offer)) returns Future.failed(new RuntimeException("test exception"))
//      offerService.createOffer(offerDraft) must be (Option.empty[Offer]).await
//    }
//  }

}

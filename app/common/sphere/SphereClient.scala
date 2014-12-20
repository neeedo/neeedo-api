package common.sphere

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Locale

import io.sphere.sdk.attributes.AttributeDefinition
import io.sphere.sdk.client.{SphereRequestExecutorTestDouble, ScalaClientImpl, ScalaClient}
import io.sphere.sdk.http.ClientRequest
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.products._
import io.sphere.sdk.producttypes.{ProductTypeBuilder, ProductType}
import io.sphere.sdk.queries.PagedQueryResult
import play.api.Play

sealed trait SphereClient {
  lazy val client = createSphereClient()
  def createSphereClient(): ScalaClient

  def execute[T](req: ClientRequest[T]) = client.execute(req)
}

object RemoteSphereClient extends SphereClient {
  override def createSphereClient(): ScalaClient = new ScalaClientImpl(Play.current.configuration.underlying)
}

object ProjectMockSphereClient extends SphereClient {
  override def createSphereClient(): ScalaClient = new ScalaClientImpl(Play.current.configuration.underlying, new ProductTestClient)

  class ProductTestClient extends SphereRequestExecutorTestDouble {


    override def result[T](requestable: ClientRequest[T]): T = {
      val productType: ProductType = createProductTypeMock
      val product = createProductMock(productType)

      if(requestable.isInstanceOf[ClientRequest[Product]]) {
        PagedQueryResult.of(product).asInstanceOf[T]
      }
      else super.result(requestable)
    }

    def createProductTypeMock: ProductType = {
      val createdAt: Instant = Instant.parse("2001-09-11T14:00:00.000Z")
      val lastModifiedAt: Instant = createdAt.plus(2, ChronoUnit.HOURS)
      val productType: ProductType = ProductTypeBuilder
        .of("1", "T-Shirts", "Tolle T-Shirts", new java.util.ArrayList[AttributeDefinition])
        .createdAt(createdAt)
        .lastModifiedAt(lastModifiedAt)
        .version(1)
        .build()

      productType
    }

    def createProductMock(productType: ProductType) = {
      val emptyProductVariant = ProductVariantBuilder.of(1).sku("sku-5000").get()
      val name: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, "name")
      val slug: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, "slug")
      val description: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, "description")
      val staged = ProductDataBuilder.of(name, slug, emptyProductVariant).description(description).build()
      val masterData = ProductCatalogDataBuilder.ofStaged(staged).get()

      ProductBuilder.of(productType, masterData).id("foo-id").build()
    }
  }

}



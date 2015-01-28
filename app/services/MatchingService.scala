package services

import common.domain._
import common.sphere.{ProductTypes, SphereClient}
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.{PagedQueryResult, QueryDsl}
import model.{DemandId, Demand}
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingService(sphereClient: SphereClient, productTypes: ProductTypes) {
  def matchDemands(): Future[List[Demand]] = {
    val query: QueryDsl[Product] = ProductQuery.of().byProductType(productTypes.demand)
    val result: Future[PagedQueryResult[Product]] = sphereClient.execute(query)

    result.map(pagedQueryResult => pagedQueryResult.getResults.asScala.toList.map(productToDemand))
  }

  def productToDemand(product: Product): Demand = {
    Demand(
      DemandId(product.getId),
      Version(product.getVersion),
      UserId(getAttribute(product, "userId").getValue(AttributeAccess.ofString().attributeMapper())),
      getAttribute(product, "tags").getValue(AttributeAccess.ofString().attributeMapper()),
      Location(
        Longitude(getAttribute(product, "longitude").getValue(AttributeAccess.ofDouble().attributeMapper())),
        Latitude(getAttribute(product, "latitude").getValue(AttributeAccess.ofDouble().attributeMapper()))
      ),
      Distance(getAttribute(product, "distance").getValue(AttributeAccess.ofDouble().attributeMapper()).intValue()),
      // Todo Nullpointer case
      Price(getAttribute(product, "priceMin").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue()),
      Price(getAttribute(product, "priceMax").getValue(AttributeAccess.ofMoney().attributeMapper()).getNumber.doubleValue())
    )
  }

  def getAttribute(product: Product, name: String) =
    product.getMasterData.getStaged.getMasterVariant.getAttribute(name).get()
}

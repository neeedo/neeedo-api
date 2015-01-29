package services

import common.sphere.{ProductTypes, SphereClient}
import io.sphere.sdk.products.Product
import io.sphere.sdk.products.queries.ProductQuery
import io.sphere.sdk.queries.{PagedQueryResult, QueryDsl}
import model.Demand
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MatchingService(sphereClient: SphereClient, productTypes: ProductTypes) {
  def matchDemands(): Future[List[Demand]] = {
    val query: QueryDsl[Product] = ProductQuery.of().byProductType(productTypes.demand)
    val result: Future[PagedQueryResult[Product]] = sphereClient.execute(query)

    result.map(pagedQueryResult => pagedQueryResult.getResults.asScala.toList.map(Demand.productToDemand).flatten)
  }
}
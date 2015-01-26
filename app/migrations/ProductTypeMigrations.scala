package migrations

import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.producttypes.{ProductTypeDraft, ProductType}
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.ImplicitConversions._

import scala.concurrent.Future

class ProductTypeMigrations(sphereClient: SphereClient) extends Migration {

  override def run(): Unit = {
    createType(ProductTypeDrafts.demand)
    createType(ProductTypeDrafts.offer)
  }

  def createType(typeDraft: ProductTypeDraft): Unit = {
    val typeName = typeDraft.getName
    val queryResult: Future[PagedQueryResult[ProductType]] = sphereClient.execute(ProductTypeQuery.of().byName(typeName))
    val option: Future[Option[ProductType]] = queryResult.map(res => res.head())

    option.map {
      case None =>
        Logger.info(s"Cannot find $typeName Product Type. Creating type $typeName...")
        val createCommand = ProductTypeCreateCommand.of(typeDraft)
        sphereClient.execute(createCommand)
      case Some(prodType: ProductType) => Logger.info(s"Found $typeName Product Type($typeName).")
    }
  }
}
package migrations

import common.logger.MigrationsLogger
import common.sphere.{ProductTypeDrafts, SphereClient}
import io.sphere.sdk.producttypes.{ProductTypeDraft, ProductType}
import io.sphere.sdk.producttypes.commands.ProductTypeCreateCommand
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import common.helper.ImplicitConversions._

import scala.concurrent.Future

class ProductTypeMigrations(sphereClient: SphereClient, productTypeDrafts: ProductTypeDrafts) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Product Type Migrations started")

    for {
      demand <- createType(productTypeDrafts.demand)
      offer <- createType(productTypeDrafts.offer)
    } yield(demand, offer)
  }

  def createType(typeDraft: ProductTypeDraft): Future[Unit] = {
    val typeName = typeDraft.getName
    val queryResult: Future[PagedQueryResult[ProductType]] = sphereClient.execute(ProductTypeQuery.of().byName(typeName))
    val option: Future[Option[ProductType]] = queryResult.map(res => res.head().asScala)

    option.flatMap {
      case None =>
        val createCommand = ProductTypeCreateCommand.of(typeDraft)
        sphereClient.execute(createCommand).map(res => MigrationsLogger.info(s"-> Cannot find $typeName Product Type. Creating type $typeName..."))
      case Some(prodType: ProductType) => Future.successful(MigrationsLogger.info(s"-> Found $typeName Product Type($typeName)."))
    }
  }
}
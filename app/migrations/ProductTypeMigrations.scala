package migrations

import common.helper.Configloader
import common.sphere.{SphereClient}
import io.sphere.sdk.producttypes.{ProductTypeDraft, ProductType}
import io.sphere.sdk.producttypes.commands.{ProductTypeCreateCommand}
import io.sphere.sdk.producttypes.queries.ProductTypeQuery
import io.sphere.sdk.queries.PagedQueryResult
import model.sphere.{ProductTypeFactory}
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ProductTypeMigrations(sphereClient: SphereClient) extends Migration {

  implicit def optionalToOption[T](opt: java.util.Optional[T]): Option[T] = {
    if (opt.isPresent) Some[T](opt.get())
    else Option.empty[T]
  }

  override def run(): Unit = {
    createType("demand", ProductTypeFactory.demandType)
    createType("offer", ProductTypeFactory.offerType)
  }

  def createType(configKey: String, typeDraft: ProductTypeDraft) = {
    val typeName = Configloader.getStringOpt(s"$configKey.typeName").get
    val queryResult: Future[PagedQueryResult[ProductType]] = sphereClient.execute(ProductTypeQuery.of().byName(typeName))
    val option: Future[Option[ProductType]] = queryResult.map(res => res.head())

    option.map {
      case None =>
        val createCommand = ProductTypeCreateCommand.of(typeDraft)
        sphereClient.execute(createCommand)
        Logger.info(s"Cannot find $typeName Product Type. Creating type $typeName...")
      case Some(prodType: ProductType) => Logger.info(s"Found $typeName Product Type($typeName).")
    }
  }
}
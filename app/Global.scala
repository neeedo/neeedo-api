import java.util.concurrent.TimeUnit

import common.elasticsearch.ElasticsearchClientFactory
import common.helper.{Wirehelper, CrossOriginFilter}
import common.logger.MigrationsLogger
import migrations.{CompletionsEsMigrations, ProductTypeEsMigrations, ProductTestDataMigrations, ProductTypeMigrations}
import play.api._
import com.softwaremill.macwire.{MacwireMacros, Macwire}
import play.api.mvc.WithFilters
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Global extends WithFilters(CrossOriginFilter) with GlobalSettings with Macwire  {


  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    Wirehelper().lookupSingleOrThrow(controllerClass)
  }

  override def onStop(app: Application): Unit = {
    Wirehelper().lookupSingleOrThrow(classOf[ElasticsearchClientFactory]).instance.close()
  }

  override def onStart(app: Application): Unit = {
    if (Play.current.mode != Mode.Test) migrations()
  }

  def migrations() = {
    MigrationsLogger.info("### Migrations started ###")
    Await.result(Wirehelper().lookupSingleOrThrow(classOf[ProductTypeMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(Wirehelper().lookupSingleOrThrow(classOf[ProductTypeEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(Wirehelper().lookupSingleOrThrow(classOf[CompletionsEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(Wirehelper().lookupSingleOrThrow(classOf[ProductTestDataMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    MigrationsLogger.info("### Migrations done ###\n")
  }
}


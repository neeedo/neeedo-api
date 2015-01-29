import java.util.concurrent.TimeUnit

import common.elasticsearch.ElasticsearchClientFactory
import migrations.{ProductTestDataMigrations, ProductTypeMigrations}
import play.api._
import com.softwaremill.macwire.{MacwireMacros, Macwire}
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

object Global extends GlobalSettings with Macwire {
  import MacwireMacros._
  val wired = wiredInModule(new WireDependencies {})

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    wired.lookupSingleOrThrow(controllerClass)
  }

  override def onStop(app: Application): Unit = {
    ElasticsearchClientFactory.instance.close()
  }

  override def onStart(app: Application): Unit = {
    if (Play.current.mode != Mode.Test) {
      Await.result(wired.lookupSingleOrThrow(classOf[ProductTypeMigrations]).run(), new FiniteDuration(10, TimeUnit.SECONDS))
      Await.result(wired.lookupSingleOrThrow(classOf[ProductTestDataMigrations]).run(), new FiniteDuration(10, TimeUnit.SECONDS))
    }
  }
}


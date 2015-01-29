import common.elasticsearch.ElasticsearchClientFactory
import migrations.{ProductTestDataMigrations, ProductTypeMigrations}
import play.api.{Mode, Play, Application, GlobalSettings}
import com.softwaremill.macwire.{MacwireMacros, Macwire}

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
      wired.lookupSingleOrThrow(classOf[ProductTypeMigrations]).run()
      wired.lookupSingleOrThrow(classOf[ProductTestDataMigrations]).run()
    }
  }
}

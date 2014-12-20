import common.elasticsearch.ElasticsearchClientFactory
import play.api.{Application, GlobalSettings}
import com.softwaremill.macwire.{MacwireMacros, Macwire}

object Global extends GlobalSettings with Macwire {
  import MacwireMacros._
  lazy val wired = wiredInModule(new WireDependencies {})

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    wired.lookupSingleOrThrow(controllerClass)
  }

  override def onStop(app: Application): Unit = {
    ElasticsearchClientFactory.getInstance.close
  }
}

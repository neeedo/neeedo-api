//package common
//
//import java.util.concurrent.TimeUnit
//
//import com.softwaremill.macwire.Macwire
//import common.elasticsearch.ElasticsearchClientFactory
//import common.helper.{CrossOriginFilter, WireDependencies}
//import common.logger.MigrationsLogger
//import migrations._
//import play.api._
//import play.api.mvc.{RequestHeader, WithFilters}
//
//import scala.concurrent.Await
//import scala.concurrent.duration.FiniteDuration
//
//object Global extends WithFilters(CrossOriginFilter) with GlobalSettings with Macwire  {
//  lazy val wired = wiredInModule(new WireDependencies {})
//
//  override def getControllerInstance[A](controllerClass: Class[A]): A = {
//    wired.lookupSingleOrThrow(controllerClass)
//  }
//
//  override def onStop(app: Application): Unit = {
//    wired.lookupSingleOrThrow(classOf[ElasticsearchClientFactory]).instance.close()
//  }
//
//  override def onStart(app: Application): Unit = {
//    if (Play.current.mode != Mode.Test) migrations()
//  }
//
//  override def onError(request: RequestHeader, e: Throwable) = {
//    Logger.error(e.getMessage, e)
//    super.onError(request, e)
//  }
//
//  def migrations() = {
//    MigrationsLogger.info("### Migrations started ###")
//    Await.result(wired.lookupSingleOrThrow(classOf[ProductTypeMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[ProductTypeEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[CompletionsEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[MessagesEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[ProductTestDataMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[AmazonS3Migrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    Await.result(wired.lookupSingleOrThrow(classOf[FavoritesEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
//    MigrationsLogger.info("### Migrations done ###\n")
//  }
//}
//

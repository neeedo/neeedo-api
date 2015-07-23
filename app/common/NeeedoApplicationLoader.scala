package common

import com.softwaremill.macwire.MacwireMacros._
import controllers.Assets
import play.api.ApplicationLoader.Context
import play.api._
import play.api.i18n._
import play.api.routing.Router
import router.Routes


class NeeedoApplicationLoader extends  ApplicationLoader {
//  if (Play.current.mode != Mode.Test) migrations()

  def load(context: Context) = {
    def load(context: Context): Application = {
      Logger.configure(context.environment)
      (new BuiltInComponentsFromContext(context) with NeeedoComponents).application
    }
  }



//  def migrations() = {
//    val wired = wiredInModule(new WireDependencies {})
//
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
}

trait NeeedoComponents extends BuiltInComponents with NeeedoModule with I18nComponents {
  lazy val assets: Assets = wire[Assets]
  lazy val router: Router = wire[Routes] withPrefix "/"
}
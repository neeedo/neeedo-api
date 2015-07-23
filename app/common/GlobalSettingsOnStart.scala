package common

import java.util.concurrent.TimeUnit

import common.Global._
import common.helper.WireDependencies
import common.logger.MigrationsLogger
import migrations._
import play.api.{Mode, Play}

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

class GlobalSettingsOnStart {
  if (Play.current.mode != Mode.Test) migrations()

  def migrations() = {
    val wired = wiredInModule(new WireDependencies {})

    MigrationsLogger.info("### Migrations started ###")
    Await.result(wired.lookupSingleOrThrow(classOf[ProductTypeMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[ProductTypeEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[CompletionsEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[MessagesEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[ProductTestDataMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[AmazonS3Migrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    Await.result(wired.lookupSingleOrThrow(classOf[FavoritesEsMigrations]).run(), new FiniteDuration(30, TimeUnit.SECONDS))
    MigrationsLogger.info("### Migrations done ###\n")
  }
}

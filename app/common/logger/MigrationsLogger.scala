package common.logger

import org.slf4j.{LoggerFactory, Logger}
import play.api.LoggerLike

object MigrationsLogger extends LoggerLike {
  override val logger: Logger = LoggerFactory.getLogger("Migrations")
}
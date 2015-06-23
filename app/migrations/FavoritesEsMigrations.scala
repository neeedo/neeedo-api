package migrations

import common.elasticsearch.{EsMapping, ElasticsearchClient}
import common.helper.ConfigLoader
import common.logger.MigrationsLogger

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class FavoritesEsMigrations(elasticsearch: ElasticsearchClient, config: ConfigLoader) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Favorites Elasticsearch Index Migrations started")
    elasticsearch.waitForGreenStatus.flatMap(green => {
      if (green) {
        MigrationsLogger.info("# Elasticsearch cluster status is green")

        val indexRequest = elasticsearch.buildIndexRequest(
          config.favoritesIndex,
          EsMapping(config.favoritesIndex.toTypeName, "migrations/favorites-mapping.json"))

        elasticsearch.createIndex(config.favoritesIndex, indexRequest).map(_ => Unit)
      } else Future(MigrationsLogger.info("# Elasticsearch cluster is not green, aborting Migration"))
    })
  }
}

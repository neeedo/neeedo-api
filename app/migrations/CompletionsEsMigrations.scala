package migrations

import common.elasticsearch.{ElasticsearchClient, EsMapping}
import common.helper.ConfigLoader
import common.logger.MigrationsLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompletionsEsMigrations(elasticsearch: ElasticsearchClient, config: ConfigLoader) extends Migration {
  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Completions Elasticsearch Index Migrations started")
    elasticsearch.waitForGreenStatus.flatMap(green => {
      if (green) {
        MigrationsLogger.info("# Elasticsearch cluster status is green")

        val indexRequest = elasticsearch.buildIndexRequest(
          config.completionsIndex,
          EsMapping(config.completionsIndex.toTypeName, "migrations/completions-mapping.json"))

        elasticsearch.createIndex(config.completionsIndex, indexRequest).map(_ => Unit)
      } else Future(MigrationsLogger.info("# Elasticsearch cluster is not green, aborting Migration"))
    })
  }


}

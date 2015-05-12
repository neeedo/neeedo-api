package migrations

import common.domain.IndexName
import common.elasticsearch.{EsIndices, ElasticsearchClient, EsMapping}
import common.helper.Configloader
import common.logger.MigrationsLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompletionsEsMigrations(elasticsearch: ElasticsearchClient) extends Migration {
  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Completions Elasticsearch Index Migrations started")

    val completionsIndex: IndexName = EsIndices.completionsIndexName
    elasticsearch.waitForGreenStatus.flatMap(green => {
      if (green) {
        MigrationsLogger.info("# Elasticsearch cluster status is green")
        for {
          completions <- elasticsearch.createIndex(completionsIndex, elasticsearch.buildIndexRequest(
            completionsIndex, EsMapping(completionsIndex.toTypeName, "migrations/completions-mapping.json")))
        } yield completions
      } else Future.successful(MigrationsLogger.info("# Elasticsearch cluster is not green, aborting Migration"))
    })
  }


}

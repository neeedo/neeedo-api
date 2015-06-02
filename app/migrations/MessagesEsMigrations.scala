package migrations

import common.domain.IndexName
import common.elasticsearch.{EsMapping, ElasticsearchClient}
import common.helper.ConfigLoader
import common.logger.MigrationsLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MessagesEsMigrations(elasticsearch: ElasticsearchClient, config: ConfigLoader) extends Migration {

  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Messages Elasticsearch Index Migrations started")
    val messagesIndex: IndexName = config.messagesIndex

    elasticsearch.waitForGreenStatus.flatMap(green => {
      if (green) {
        MigrationsLogger.info("# Elasticsearch cluster status is green")

        elasticsearch.createIndex(messagesIndex, elasticsearch.buildIndexRequest(
            messagesIndex, EsMapping(messagesIndex.toTypeName, "migrations/messages-mapping.json"))
        ).map(_ => Unit)

      } else Future.successful(MigrationsLogger.info("# Elasticsearch cluster is not green, aborting Migration"))
    })
  }

}

package migrations

import common.domain.IndexName
import common.elasticsearch.{EsSettings, ElasticsearchClient, EsMapping}
import common.helper.ConfigLoader
import common.logger.MigrationsLogger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ProductTypeEsMigrations(elasticsearch: ElasticsearchClient, config: ConfigLoader) extends Migration {
  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Producttype Elasticsearch Index Migrations started")

    val demandIndex: IndexName = config.demandIndex
    val offerIndex: IndexName = config.offerIndex
    elasticsearch.waitForGreenStatus.flatMap(green => {
      if (green) {

        MigrationsLogger.info("# Elasticsearch cluster status is green")
        for {
          demand <- elasticsearch.createIndex(demandIndex, elasticsearch.buildIndexRequest(
            demandIndex, EsMapping(demandIndex.toTypeName, "migrations/demand-mapping.json"), EsSettings("migrations/offer-demand-settings.json")))
          offer <- elasticsearch.createIndex(offerIndex, elasticsearch.buildIndexRequest(
            offerIndex, EsMapping(offerIndex.toTypeName, "migrations/offer-mapping.json"), EsSettings("migrations/offer-demand-settings.json")))
        } yield {
          (demand, offer)
        }
      } else Future.successful(MigrationsLogger.info("# Elasticsearch cluster is not green, aborting Migration"))
    })
  }


}

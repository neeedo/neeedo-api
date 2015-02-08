package migrations

import common.domain.IndexName
import common.elasticsearch.{EsMapping, ElasticsearchClient}
import common.helper.Configloader
import common.logger.MigrationsLogger
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

class ProductTypeEsMigrations(elasticsearch: ElasticsearchClient) extends Migration {
  override def run(): Future[Unit] = {
    MigrationsLogger.info("# Product Elasticsearch Index Migrations started")

    val demandIndex: IndexName = IndexName(Configloader.getString("demand.typeName"))
    val offerIndex: IndexName = IndexName(Configloader.getString("offer.typeName"))

    for {
      demand <- elasticsearch.createIndex(elasticsearch.buildIndexRequest(
        demandIndex, EsMapping(demandIndex.toTypeName, "migrations/demand-mapping.json")))
      offer <- elasticsearch.createIndex(elasticsearch.buildIndexRequest(
        offerIndex, EsMapping(offerIndex.toTypeName, "migrations/offer-mapping.json")))
    } yield(demand, offer)
  }


}

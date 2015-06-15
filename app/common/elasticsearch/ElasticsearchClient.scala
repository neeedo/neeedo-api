package common.elasticsearch

import java.util.concurrent.TimeUnit

import common.domain.{IndexName, TypeName}
import common.helper.ConfigLoader
import common.logger.EsLogger
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.{Settings, ImmutableSettings}
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query._
import org.elasticsearch.node.{Node, NodeBuilder}
import play.api.libs.json.{Json, Reads, JsValue}
import common.helper.ImplicitConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

sealed trait ElasticsearchClient {

  lazy val client = createElasticsearchClient()
  def close(): Unit
  def createElasticsearchClient(): Client

  def searchresponseAs[T](resp: SearchResponse)(implicit reads: Reads[T]): List[T] = {
    resp.getHits.getHits.map(hit => Json.parse(hit.sourceAsString()).as[T]).toList
  }

  def indexDocument(id: String, esIndex: IndexName, esType: TypeName, doc: JsValue): Future[IndexResponse] =
    client
      .prepareIndex(esIndex.value, esType.value)
      .setSource(doc.toString())
      .setId(id)
      .execute()
      .asScala

  // Todo get doc as param
  def updateDocument(id: String, esIndex: IndexName, esType: TypeName): Future[UpdateResponse] =
    client
      .prepareUpdate(esIndex.value, esType.value, id)
      .setDoc("read", true)
      .execute
      .asScala

  def search(esIndex: IndexName, esType: TypeName, query: QueryBuilder): Future[SearchResponse] =
    client
      .prepareSearch(esIndex.value)
      .setTypes(esType.value)
      .setQuery(query)
      .execute()
      .asScala

  def createIndex(indexName: IndexName, indexRequest: CreateIndexRequestBuilder): Future[Boolean] = {
    client
      .admin()
      .indices()
      .prepareExists(indexName.value)
      .execute()
      .asScala
      .flatMap {
        result =>
          if (result.isExists) Future.successful(false)
          else indexRequest.execute().asScala.map(_.isAcknowledged)
        }
  }

  def deleteDocument(id: String, indexName: IndexName, typeName: TypeName): Future[Boolean] = {
    client
      .prepareDelete(
        indexName.value,
        typeName.value,
        id)
      .execute()
      .asScala
      .map {
      result => result.isFound
    }.recover {
      case _: Exception => false
    }
  }

  def waitForGreenStatus =
    client
      .admin()
      .cluster()
      .prepareHealth()
      .setWaitForGreenStatus()
      .setTimeout(new TimeValue(20, TimeUnit.SECONDS))
      .execute()
      .asScala
      .map(res => !res.isTimedOut)

  def buildIndexRequest(index: IndexName, mapping: EsMapping): CreateIndexRequestBuilder = {
    client
      .admin()
      .indices()
      .prepareCreate(index.value)
      .setSettings(
        ImmutableSettings.settingsBuilder()
          .put("number_of_shards", 1)
          .put("number_of_replicas", 0)
          .build()
      )
      .addMapping(mapping.name.value, mapping.value)
  }
}

class LocalEsClient extends ElasticsearchClient {
  val nodeSettings = ImmutableSettings.settingsBuilder()
    .classLoader(classOf[Settings].getClassLoader)
    .put("path.data", "resources/es-data/")
    .put("path.conf", "resources/es-config/")
    .build()

  lazy val node: Node = {
    EsLogger.info(s"Build Local NodeClient")
    NodeBuilder.nodeBuilder().local(true).settings(nodeSettings).node()
  }
  override def createElasticsearchClient(): Client = node.client()
  override def close() = node.close()
}

class RemoteEsClient(configloader: ConfigLoader) extends ElasticsearchClient {
  val clustername = configloader.getStringOpt("elasticsearch.clustername").getOrElse("elasticsearch")
  val hosts = readHostsFromConfig.mkString(",")
  lazy val node: Node = createNode

  def createNode = {
    EsLogger.info(s"Build NodeClient with Hosts: $hosts and clusternamer: $clustername")
    NodeBuilder.nodeBuilder()
      .clusterName(clustername)
      .settings(ImmutableSettings.settingsBuilder()
      .classLoader(classOf[Settings].getClassLoader)
      .put("discovery.zen.ping.unicast.hosts", hosts)
      .put("node.name", "neeedo-client"))
      .client(true)
      .data(false)
      .node()
  }

  def readHostsFromConfig: List[HostWithPort] = {
    configloader.getStringSeq("elasticsearch.hosts").map {
      s =>
        val hostsAndPorts = s.split(":").toList
        HostWithPort(
          hostsAndPorts.dropRight(1).mkString(":").trim,
          hostsAndPorts.takeRight(1).mkString.trim.toInt
        )
    }
  }

  override def createElasticsearchClient(): Client = node.client()
  override def close() = node.close()
}

case class HostWithPort(host: String, port: Int) {
  override def toString = s"$host:$port"
}
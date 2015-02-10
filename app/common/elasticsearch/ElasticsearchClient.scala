package common.elasticsearch

import java.util.concurrent.TimeUnit

import common.domain.{IndexName, TypeName}
import common.helper.Configloader
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.{Settings, ImmutableSettings}
import org.elasticsearch.common.unit.TimeValue
import org.elasticsearch.index.query._
import org.elasticsearch.node.{Node, NodeBuilder}
import play.api.libs.json.JsValue
import common.helper.ImplicitConversions._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


sealed trait ElasticsearchClient {

  lazy val client = createElasticsearchClient()
  def close(): Unit
  def createElasticsearchClient(): Client

  def indexDocument(id: String, esIndex: IndexName, esType: TypeName, doc: JsValue): Future[IndexResponse] =
    client
      .prepareIndex(esIndex.value, esType.value)
      .setSource(doc.toString())
      .setId(id)
      .execute()
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
          .put("number_of_shards", 5)
          .put("number_of_replicas", 0)
          .build()
      )
      .addMapping(mapping.name.value, mapping.value)
  }


  def readHostsFromConfig: List[HostWithPort] = {
    Configloader.getStringSeq("elasticsearch.hosts").getOrElse(Nil).map {
      s =>
        val hostsAndPorts = s.split(":").toList
        HostWithPort(
          hostsAndPorts.dropRight(1).mkString(":").trim,
          hostsAndPorts.takeRight(1).mkString.trim.toInt
        )
    }
  }
}

class LocalEsClient extends ElasticsearchClient {
  val nodeSettings = ImmutableSettings.settingsBuilder()
    .classLoader(classOf[Settings].getClassLoader)
    .put("path.data", "resources/es-data/")
    .build()

  lazy val node: Node = NodeBuilder.nodeBuilder().local(true).settings(nodeSettings).node()
  override def createElasticsearchClient(): Client = node.client()
  override def close() = node.close()
}

class RemoteEsClient extends ElasticsearchClient {
  val clustername = Configloader.getStringOpt("elasticsearch.clustername").getOrElse("elasticsearch")
  val hosts = readHostsFromConfig
  lazy val node: Node = NodeBuilder.nodeBuilder()
    .clusterName(clustername)
    .settings(ImmutableSettings.settingsBuilder()
      .classLoader(classOf[Settings].getClassLoader)
      .put("discovery.zen.ping.unicast.hosts", hosts.mkString(","))
      .put("node.name", "neeedo-client"))
    .client(true)
    .data(false)
    .node()

  override def createElasticsearchClient(): Client = node.client()
  override def close() = node.close()
}

case class HostWithPort(host: String, port: Int) {
  override def toString = s"$host:$port"
}
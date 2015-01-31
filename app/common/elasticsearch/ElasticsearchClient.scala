package common.elasticsearch

import common.domain.{IndexName, TypeName}
import common.helper.Configloader
import org.elasticsearch.action.index.IndexResponse
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.Client
import org.elasticsearch.common.settings.{Settings, ImmutableSettings}
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.node.{Node, NodeBuilder}
import play.api.libs.json.JsValue
import common.helper.ImplicitConversions.convertListenableActionFutureToScalaFuture
import scala.concurrent.Future

sealed trait ElasticsearchClient {

  lazy val client = createElasticsearchClient()
  def close(): Unit
  def createElasticsearchClient(): Client

  def indexDocument(esIndex: IndexName, esType: TypeName, doc: JsValue): Future[IndexResponse] =
    client.prepareIndex(esIndex.value, esType.value).setSource(doc.toString()).execute()
  def search(esIndex: IndexName, esType: TypeName, query: QueryBuilder): Future[SearchResponse] =
    client.prepareSearch(esIndex.value).setTypes(esType.value).setQuery(query).execute()
}

class LocalEsClient extends ElasticsearchClient {
  val nodeSettings = ImmutableSettings.settingsBuilder()
    .classLoader(classOf[Settings].getClassLoader)
    .put("path.data", "target/es-data/")
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
    .client(true)
    .settings(ImmutableSettings.settingsBuilder()
      .classLoader(classOf[Settings].getClassLoader)
      .put("discovery.zen.ping.unicast.hosts", hosts.mkString(",")))
    .node()

  override def createElasticsearchClient(): Client = node.client()
  override def close() = node.close()

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

case class HostWithPort(host: String, port: Int) {
  override def toString = s"$host:$port"
}
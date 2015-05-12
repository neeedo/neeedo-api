package common.elasticsearch

import common.helper.ConfigLoader
import org.specs2.mutable.Specification
import play.api.{Play, Mode}
import play.api.test.{FakeApplication, WithApplication}

class ElasticsearchClientFactorySpec extends Specification {
  "ElasticsearchClientFactory" should {
    "returnClientForMode must return correct es client" in
      new WithApplication(FakeApplication(additionalConfiguration = Map("elasticsearch.dev.useRemoteClient" -> false,
        "elasticsearch.hosts" -> "node1:9300,node2:9300"))) {

        val configLoader = new ConfigLoader
        val factory = new ElasticsearchClientFactory(configLoader)

        factory.returnClientForMode(Mode.Test) must haveClass[LocalEsClient]
        factory.returnClientForMode(Mode.Dev) must haveClass[LocalEsClient]
        factory.returnClientForMode(Mode.Prod) must haveClass[RemoteEsClient]
    }

    "returnClientForMode must return remote es client in dev when elasticsearch.dev.useRemoteClient is true" in
      new WithApplication(FakeApplication(additionalConfiguration = Map("elasticsearch.dev.useRemoteClient" -> true,
        "elasticsearch.hosts" -> "node1:9300,node2:9300"))) {
        val configLoader = new ConfigLoader
        val factory = new ElasticsearchClientFactory(configLoader)
        factory.returnClientForMode(Mode.Dev) must haveClass[RemoteEsClient]
    }

    "getInstance should return LocalClient in test mode" in new WithApplication {
      val configLoader = new ConfigLoader
      val factory = new ElasticsearchClientFactory(configLoader)
      factory.instance must haveClass[LocalEsClient]
    }
  }
}

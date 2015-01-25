package common.elasticsearch

import org.specs2.mutable.Specification
import play.api.Mode
import play.api.test.{FakeApplication, WithApplication}

class ElasticsearchClientFactorySpec extends Specification {
  "ElasticsearchClientFactory" should {
    "returnClientForMode must return correct es client" in
      new WithApplication(FakeApplication(additionalConfiguration = Map("elasticsearch.dev.useRemoteClient" -> false))) {

      ElasticsearchClientFactory.returnClientForMode(Mode.Test) must haveClass[LocalEsClient]
      ElasticsearchClientFactory.returnClientForMode(Mode.Dev) must haveClass[LocalEsClient]
      ElasticsearchClientFactory.returnClientForMode(Mode.Prod) must haveClass[RemoteEsClient]
    }

    "returnClientForMode must return remote es client in dev when elasticsearch.dev.useRemoteClient is true" in
      new WithApplication(FakeApplication(additionalConfiguration = Map("elasticsearch.dev.useRemoteClient" -> true))) {

        ElasticsearchClientFactory.returnClientForMode(Mode.Dev) must haveClass[RemoteEsClient]
    }

    "getInstance should return LocalClient in test mode" in new WithApplication {
      ElasticsearchClientFactory.getInstance must haveClass[LocalEsClient]
    }
  }
}

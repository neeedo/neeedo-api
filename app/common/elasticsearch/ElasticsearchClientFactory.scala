package common.elasticsearch

import common.helper.ConfigLoader
import play.api.Mode.Mode
import play.api.{Mode, Play}

class ElasticsearchClientFactory(configloader: ConfigLoader) {
  lazy val instance = getInstance

  def returnClientForMode(mode: Mode) = mode match {
    case Mode.Dev =>
      val useRemoteClient = configloader.getBoolean("elasticsearch.dev.useRemoteClient")
      if (useRemoteClient)
        new RemoteEsClient(configloader)
      else
        new LocalEsClient
    case Mode.Prod => new RemoteEsClient(configloader)
    case Mode.Test => new LocalEsClient
  }

  private def getInstance = returnClientForMode(Play.current.mode)
}

package common.elasticsearch

import common.helper.Configloader
import play.api.Mode.Mode
import play.api.{Mode, Play}

object ElasticsearchClientFactory {
  lazy val instance = getInstance

  def apply(): ElasticsearchClient = instance

  def returnClientForMode(mode: Mode) = mode match {
    case Mode.Dev =>
      if (Configloader.getBoolean("elasticsearch.dev.useRemoteClient"))
        new RemoteTransportEsClient
      else
        new LocalEsClient
    case Mode.Prod => new RemoteTransportEsClient
    case Mode.Test => new LocalEsClient
  }

  private def getInstance = returnClientForMode(Play.current.mode)
}

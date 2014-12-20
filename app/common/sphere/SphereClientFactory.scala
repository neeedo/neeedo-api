package common.sphere

import play.api.Mode.Mode
import play.api.{Mode, Play}

object SphereClientFactory {
  lazy val instance = getInstance

  def apply(): SphereClient = instance

  def returnClientForMode(mode: Mode) = mode match {
    case Mode.Dev => RemoteSphereClient
    case Mode.Prod => RemoteSphereClient
    case Mode.Test => ProjectMockSphereClient
  }

  def getInstance = returnClientForMode(Play.current.mode)
}

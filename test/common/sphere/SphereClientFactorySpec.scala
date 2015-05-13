package common.sphere

import common.helper.ConfigLoader
import org.specs2.mutable.Specification
import play.api.Play
import play.api.test.WithApplication


class SphereClientFactorySpec extends Specification {
  "SphereClientFactory" should {
    "returnClientForMode must return correct client" in new WithApplication {
      val configLoader = new ConfigLoader

      new SphereClientFactory(configLoader).getInstance must beAnInstanceOf[RemoteSphereClient]
    }
  }
}

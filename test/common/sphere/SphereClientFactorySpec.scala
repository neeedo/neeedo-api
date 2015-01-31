package common.sphere

import org.specs2.mutable.Specification
import play.api.test.WithApplication


class SphereClientFactorySpec extends Specification {
  "SphereClientFactory" should {
    "returnClientForMode must return correct client" in new WithApplication {
      SphereClientFactory.getInstance must be(RemoteSphereClient)
    }
  }
}

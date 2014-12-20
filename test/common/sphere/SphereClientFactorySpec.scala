package common.sphere

import org.specs2.mutable.Specification
import play.api.Mode
import play.api.test.WithApplication


class SphereClientFactorySpec extends Specification {
  "SphereClientFactory" should {
    "returnClientForMode must return correct client" in new WithApplication {
      SphereClientFactory.returnClientForMode(Mode.Test) must be(ProjectMockSphereClient)
      SphereClientFactory.returnClientForMode(Mode.Dev) must be(RemoteSphereClient)
      SphereClientFactory.returnClientForMode(Mode.Prod) must be(RemoteSphereClient)
    }

    "getInstance should return MockClient" in new WithApplication {
      SphereClientFactory.getInstance must be(ProjectMockSphereClient)
    }
  }
}

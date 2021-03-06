package common.domain

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import test.TestData

class VersionSpec extends Specification {

  "Version" should {
    "must be correctly applied from pathbinder" in new WithApplication {
      Version.pathBinder.bind("key1", TestData.version.value.toString) mustEqual Right(TestData.version)
    }

    "must be correctly unapplied from pathbinder" in new WithApplication {
      Version.pathBinder.unbind("key", TestData.version) mustEqual TestData.version.value.toString
    }
  }
}

package common.domain

import org.specs2.mutable.Specification
import play.api.test.WithApplication

class VersionSpec extends Specification {

  "Version" should {
    "be correctly be created from an identifier" in new WithApplication {
      Version.pathBinder.bind("key1", "123") mustEqual Right(Version(123L))
    }

    "be correctly be transform into an identifier" in new WithApplication {
      Version.pathBinder.unbind("key", Version(123L)) mustEqual "123"
    }
  }

  "++" should {
    "return the next Version" in {
      Version(1L).++ mustEqual Version(2L)
    }
  }
}

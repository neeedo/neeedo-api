package common.domain

import org.specs2.mutable.Specification
import play.api.test.WithApplication
import test.TestData

class PageSizeSpec extends Specification {

  "PageSize" should {
    "must be correctly applied from pathbinder" in new WithApplication {
      PageSize.pathBinder.bind("key1", "0") mustEqual Right(PageSize(0))
    }

    "must be correctly unapplied from pathbinder" in new WithApplication {
      PageSize.pathBinder.unbind("key", PageSize(0)) mustEqual "0"
    }
  }
}

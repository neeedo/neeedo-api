package common.domain

import org.specs2.mutable.Specification
import play.api.test.WithApplication

class PagerSpec extends Specification {

  "Pager" should {
    "must be correctly applied from querybindable" in new WithApplication {
      val params = Map("limit" -> Seq("1"), "offset" -> Seq("3"))
      Pager.queryStringBinder.bind("key1", params) mustEqual Some(Right(Pager(1, 3)))
    }

    "must be correctly unapplied from pathbinder" in new WithApplication {
      Pager.queryStringBinder.unbind("key", Pager(1,3)) mustEqual "limit=1&offset=3"
    }
  }
}

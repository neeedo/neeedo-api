package common.domain

import org.specs2.mutable.Specification

class IndexNameSpec extends Specification {
  "IndexName" should {
    "toTypeName must return correct TypeName" in {
      IndexName("type").toTypeName must beEqualTo(TypeName("type"))
    }
  }

  "TypeName" should {
    "toIndexName must return correct IndexName" in {
      TypeName("index").toIndexName must beEqualTo(IndexName("index"))
    }
  }
}

package common.elasticsearch

import java.io.FileNotFoundException

import common.domain.TypeName
import org.specs2.mutable.Specification
import test.TestApplications

class EsMappingSpec extends Specification {

  "EsMapping" should {
    "EsMapping value must read from file" in TestApplications.loggingOffApp() {
      EsMapping(TypeName("Bla"), "nonexistingfile").value must throwA(new FileNotFoundException("nonexistingfile"))
    }
  }
}

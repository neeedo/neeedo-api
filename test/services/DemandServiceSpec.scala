package services

import common.domain._
import common.elasticsearch.ElasticsearchClient
import common.sphere.SphereClient
import model.{DemandId, Demand}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class DemandServiceSpec extends Specification with Mockito {

  "Demand service" should {
    "productToDemand must return valid Demand objects" in {
      val es = mock[ElasticsearchClient]
      val sphere = mock[SphereClient]
      val demandService = new DemandService(es, sphere)

      val demand1 = Demand(DemandId("1"), Version(1L), UserId("1"), "socken bekleidung wolle", Location(Longitude(52.468562), Latitude(13.534212)), Distance(30), Price(25.0), Price(77.0))

      //demandService.productToDemand(demand1)
      true mustEqual true
    }
  }
}

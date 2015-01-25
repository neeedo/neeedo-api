package common.sphere

import common.domain._
import io.sphere.sdk.attributes.Attribute
import io.sphere.sdk.models.DefaultCurrencyUnits
import io.sphere.sdk.utils.MoneyImpl
import org.specs2.mutable.Specification
import scala.collection.JavaConverters._

class ProductTypeDraftsSpec  extends Specification {

  "buildDemandAttributes" should {
    "return correct java.util.List<Attribute>" in {
      val demandDraft = DemandDraft(UserId("1"), "", Location(Longitude(1), Latitude(1)), Distance(1), Price(1), Price(1))

      ProductTypeDrafts.buildDemandAttributes(demandDraft) mustEqual List(
        Attribute.of("userId", demandDraft.uid.value),
        Attribute.of("tags", demandDraft.tags),
        Attribute.of("longitude", demandDraft.location.lon.value),
        Attribute.of("latitude", demandDraft.location.lat.value),
        Attribute.of("distance", demandDraft.distance.value),
        Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demandDraft.priceMin.value).bigDecimal, DefaultCurrencyUnits.EUR)),
        Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demandDraft.priceMax.value).bigDecimal, DefaultCurrencyUnits.EUR))
      ).asJava
    }
  }

}

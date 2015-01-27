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

  "buildOfferAttributes" should {
    "return correct java.util.List<Attribute>" in {
      val offerDraft = OfferDraft(UserId("1"), "", Location(Longitude(1), Latitude(1)), Price(1))

      ProductTypeDrafts.buildOfferAttributes(offerDraft) mustEqual List(
        Attribute.of("userId", offerDraft.uid.value),
        Attribute.of("tags", offerDraft.tags),
        Attribute.of("longitude", offerDraft.location.lon.value),
        Attribute.of("latitude", offerDraft.location.lat.value),
        Attribute.of("price", MoneyImpl.of(BigDecimal(offerDraft.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
      ).asJava
    }
  }
}

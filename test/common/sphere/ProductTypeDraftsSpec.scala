//package common.sphere
//
//import common.helper.ConfigLoader
//import io.sphere.sdk.attributes.Attribute
//import io.sphere.sdk.models.DefaultCurrencyUnits
//import io.sphere.sdk.utils.MoneyImpl
//import org.specs2.mutable.Specification
//import play.api.Play
//import test.{TestApplications, TestData}
//import scala.collection.JavaConverters._
//
//class ProductTypeDraftsSpec  extends Specification {
//
//  "buildDemandAttributes" should {
//    "return correct java.util.List<Attribute>" in TestApplications.loggingOffApp() {
//      val demandDraft = TestData.demandDraft
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//
//      productTypeDrafts.buildDemandAttributes(demandDraft) mustEqual List(
//        Attribute.of("userId", demandDraft.uid.value),
//        Attribute.of("mustTags", demandDraft.mustTags.mkString(";")),
//        Attribute.of("shouldTags", demandDraft.shouldTags.mkString(";")),
//        Attribute.of("longitude", demandDraft.location.lon.value),
//        Attribute.of("latitude", demandDraft.location.lat.value),
//        Attribute.of("distance", demandDraft.distance.value),
//        Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demandDraft.priceMin.value).bigDecimal, DefaultCurrencyUnits.EUR)),
//        Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demandDraft.priceMax.value).bigDecimal, DefaultCurrencyUnits.EUR))
//      ).asJava
//    }
//  }
//
//  "buildOfferAttributes" should {
//    "return correct java.util.List<Attribute>" in TestApplications.loggingOffApp() {
//      val offerDraft = TestData.offerDraft
//      val configLoader = new ConfigLoader
//      val productTypeDrafts = new ProductTypeDrafts(configLoader)
//
//      productTypeDrafts.buildOfferAttributes(offerDraft) mustEqual List(
//        Attribute.of("userId", offerDraft.uid.value),
//        Attribute.of("tags", offerDraft.tags.mkString(";")),
//        Attribute.of("longitude", offerDraft.location.lon.value),
//        Attribute.of("latitude", offerDraft.location.lat.value),
//        Attribute.of("price", MoneyImpl.of(BigDecimal(offerDraft.price.value).bigDecimal, DefaultCurrencyUnits.EUR))
//      ).asJava
//    }
//  }
//}

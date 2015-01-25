package common.sphere

import java.util.Locale

import common.domain.DemandDraft
import common.helper.Configloader
import io.sphere.sdk.attributes._
import io.sphere.sdk.models.{DefaultCurrencyUnits, LocalizedStrings}
import io.sphere.sdk.producttypes.ProductTypeDraft
import io.sphere.sdk.utils.MoneyImpl
import scala.collection.JavaConverters._

object ProductTypeDrafts {
  val demand = ProductTypeDraft.of(Configloader.getStringOpt("demand.typeName").get, "desc", demandAttributes)
  val offer = ProductTypeDraft.of(Configloader.getStringOpt("offer.typeName").get, "desc", offerAttributes)

  def buildDemandAttributes(demandDraft: DemandDraft) = List(
    Attribute.of("userId", demandDraft.uid.value),
    Attribute.of("tags", demandDraft.tags),
    Attribute.of("longitude", demandDraft.location.lon.value),
    Attribute.of("latitude", demandDraft.location.lat.value),
    Attribute.of("distance", demandDraft.distance.value),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demandDraft.priceMin.value).bigDecimal, DefaultCurrencyUnits.EUR)),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demandDraft.priceMax.value).bigDecimal, DefaultCurrencyUnits.EUR))
  ).asJava

  private def demandAttributes: java.util.List[AttributeDefinition] =
    List(userId, tags, longitude, latitude, distance, priceMin, priceMax).asJava

  private def offerAttributes: java.util.List[AttributeDefinition] =
    List(userId, tags, longitude, latitude, price).asJava

  private def userId: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def tags: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def longitude: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("longitude", LocalizedStrings.of(Locale.ENGLISH, "longitude"))
    .isRequired(true)
    .build()

  private def latitude: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("latitude", LocalizedStrings.of(Locale.ENGLISH, "latitude"))
    .isRequired(true)
    .build()

  private def distance: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"))
    .isRequired(false)
    .build()

  private def price: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("price", LocalizedStrings.of(Locale.ENGLISH, "price"))
    .isRequired(false)
    .build()

  private def priceMin: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"))
    .isRequired(false)
    .build()

  private def priceMax: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("priceMax", LocalizedStrings.of(Locale.ENGLISH, "priceMax"))
    .isRequired(false)
    .build()
}
package common.sphere

import java.util.Locale

import common.domain.{OfferDraft, DemandDraft}
import common.helper.ConfigLoader
import io.sphere.sdk.attributes._
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.producttypes.ProductTypeDraft
import io.sphere.sdk.utils.MoneyImpl
import scala.collection.JavaConverters._

class ProductTypeDrafts(configloader: ConfigLoader) {
  lazy val demand = ProductTypeDraft
    .of(configloader.getString("demand.typeName"),
      "desc",
      demandAttributes)
  lazy val offer = ProductTypeDraft
    .of(configloader.getString("offer.typeName"),
      "desc",
      offerAttributes)

  //TODO move later to refactored demandservice (dumb doman classes, clever models, clever services)
  def buildDemandAttributes(demandDraft: DemandDraft) = List(
    Attribute.of("userId", demandDraft.uid.value),
    Attribute.of("mustTags", demandDraft.mustTags.mkString(";")),
    Attribute.of("shouldTags", demandDraft.shouldTags.mkString(";")),
    Attribute.of("longitude", demandDraft.location.lon.value),
    Attribute.of("latitude", demandDraft.location.lat.value),
    Attribute.of("distance", demandDraft.distance.value),
    Attribute.of("priceMin", MoneyImpl.of(BigDecimal(demandDraft.priceMin.value).bigDecimal, "EUR")),
    Attribute.of("priceMax", MoneyImpl.of(BigDecimal(demandDraft.priceMax.value).bigDecimal, "EUR"))
  ).asJava

  private def demandAttributes: java.util.List[AttributeDefinition] =
    List(userId, mustTags, shouldTags, longitude, latitude, distance, priceMin, priceMax).asJava

  private def offerAttributes: java.util.List[AttributeDefinition] =
    List(userId, tags, longitude, latitude, price).asJava

  private def userId: AttributeDefinition = AttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def tags: AttributeDefinition = AttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def mustTags: AttributeDefinition = AttributeDefinitionBuilder
    .of("mustTags", LocalizedStrings.of(Locale.ENGLISH, "mustTags"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def shouldTags: AttributeDefinition = AttributeDefinitionBuilder
    .of("shouldTags", LocalizedStrings.of(Locale.ENGLISH, "shouldTags"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def longitude: AttributeDefinition = AttributeDefinitionBuilder
    .of("longitude", LocalizedStrings.of(Locale.ENGLISH, "longitude"), NumberType.of())
    .isRequired(true)
    .build()

  private def latitude: AttributeDefinition = AttributeDefinitionBuilder
    .of("latitude", LocalizedStrings.of(Locale.ENGLISH, "latitude"), NumberType.of())
    .isRequired(true)
    .build()

  private def distance: AttributeDefinition = AttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"), NumberType.of())
    .isRequired(false)
    .build()

  private def price: AttributeDefinition = AttributeDefinitionBuilder
    .of("price", LocalizedStrings.of(Locale.ENGLISH, "price"), MoneyType.of())
    .isRequired(false)
    .build()

  private def priceMin: AttributeDefinition = AttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"), MoneyType.of())
    .isRequired(false)
    .build()

  private def priceMax: AttributeDefinition = AttributeDefinitionBuilder
    .of("priceMax", LocalizedStrings.of(Locale.ENGLISH, "priceMax"), MoneyType.of())
    .isRequired(false)
    .build()
}
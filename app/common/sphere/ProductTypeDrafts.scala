package common.sphere

import java.util.Locale

import common.domain.{DemandDraft}
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

  private def demandAttributes: java.util.List[AttributeDefinition] =
    List(userId, userName, mustTags, shouldTags, longitude, latitude, distance, priceMin, priceMax).asJava

  private def offerAttributes: java.util.List[AttributeDefinition] =
    List(userId, userName, tags, longitude, latitude, price, images).asJava

  private def userId: AttributeDefinition = AttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def userName: AttributeDefinition = AttributeDefinitionBuilder
    .of("userName", LocalizedStrings.of(Locale.ENGLISH, "userName"), TextType.of())
    .isRequired(true)
    .inputHint(TextInputHint.SINGLE_LINE)
    .build()

  private def tags: AttributeDefinition = AttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), SetType.of(TextType.of()))
    .isSearchable(false)
    .build()

  private def mustTags: AttributeDefinition = AttributeDefinitionBuilder
    .of("mustTags", LocalizedStrings.of(Locale.ENGLISH, "mustTags"), SetType.of(TextType.of()))
    .isSearchable(false)
    .build()

  private def shouldTags: AttributeDefinition = AttributeDefinitionBuilder
    .of("shouldTags", LocalizedStrings.of(Locale.ENGLISH, "shouldTags"), SetType.of(TextType.of()))
    .isSearchable(false)
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

  private def images: AttributeDefinition = AttributeDefinitionBuilder
    .of("images", LocalizedStrings.of(Locale.ENGLISH, "images"), SetType.of(TextType.of()))
    .isSearchable(false)
    .build()

}
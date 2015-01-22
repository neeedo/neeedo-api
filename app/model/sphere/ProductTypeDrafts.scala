package model.sphere

import common.helper.Configloader
import io.sphere.sdk.attributes.AttributeDefinition
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.producttypes.ProductTypeDraft
import io.sphere.sdk.attributes._
import java.util.Locale
import scala.collection.JavaConverters._

object ProductTypeDrafts {

  val demand = ProductTypeDraft.of(Configloader.getStringOpt("demand.typeName").get, "desc", demandAttributes)
  val offer = ProductTypeDraft.of(Configloader.getStringOpt("offer.typeName").get, "desc", offerAttributes)

  private def demandAttributes: java.util.List[AttributeDefinition] =
    List(id ,  userId ,  productId ,  tags ,  longitude ,  latitude ,  distance ,  priceMin ,  priceMax).asJava

  private def offerAttributes: java.util.List[AttributeDefinition] =
    List(id, userId, tags, longitude, latitude, price).asJava

  private val id: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("id", LocalizedStrings.of(Locale.ENGLISH, "id"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val userId: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val productId: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("productId", LocalizedStrings.of(Locale.ENGLISH, "productId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val tags: AttributeDefinition = TextAttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val longitude: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("longitude", LocalizedStrings.of(Locale.ENGLISH, "longitude"))
    .isRequired(true)
    .build()

  private val latitude: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("latitude", LocalizedStrings.of(Locale.ENGLISH, "latitude"))
    .isRequired(true)
    .build()

  private val distance: AttributeDefinition = NumberAttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"))
    .isRequired(false)
    .build()

  private val price: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("price", LocalizedStrings.of(Locale.ENGLISH, "price"))
    .isRequired(false)
    .build()

  private val priceMin: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"))
    .isRequired(false)
    .build()

  private val priceMax: AttributeDefinition = MoneyAttributeDefinitionBuilder
    .of("priceMax", LocalizedStrings.of(Locale.ENGLISH, "priceMax"))
    .isRequired(false)
    .build()
}
package model.sphere

import common.helper.Configloader
import io.sphere.sdk.attributes.AttributeDefinition
import io.sphere.sdk.models.LocalizedStrings
import io.sphere.sdk.producttypes.ProductTypeDraft
import io.sphere.sdk.attributes._
import java.util.Locale
import scala.collection.JavaConversions._

object ProductTypeDrafts {

  val demand = ProductTypeDraft.of(Configloader.getStringOpt("demand.typeName").get, "desc", demandAttributes)
  val offer = ProductTypeDraft.of(Configloader.getStringOpt("offer.typeName").get, "desc", offerAttributes)

  private def demandAttributes: java.util.List[AttributeDefinition] =
    id :: userId :: productId :: tags :: longitude :: latitude :: distance :: priceMin :: priceMax :: Nil

  private def offerAttributes: java.util.List[AttributeDefinition] =
    id :: userId :: tags :: longitude :: latitude :: price :: Nil

  private def id = TextAttributeDefinitionBuilder
    .of("id", LocalizedStrings.of(Locale.ENGLISH, "id"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def userId = TextAttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def productId = TextAttributeDefinitionBuilder
    .of("productId", LocalizedStrings.of(Locale.ENGLISH, "productId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def tags = TextAttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private def longitude = NumberAttributeDefinitionBuilder
    .of("longitude", LocalizedStrings.of(Locale.ENGLISH, "longitude"))
    .isRequired(true)
    .build()

  private def latitude = NumberAttributeDefinitionBuilder
    .of("latitude", LocalizedStrings.of(Locale.ENGLISH, "latitude"))
    .isRequired(true)
    .build()

  private def distance = NumberAttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"))
    .isRequired(false)
    .build()

  private def price = MoneyAttributeDefinitionBuilder
    .of("price", LocalizedStrings.of(Locale.ENGLISH, "price"))
    .isRequired(false)
    .build()

  private def priceMin = MoneyAttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"))
    .isRequired(false)
    .build()

  private def priceMax = MoneyAttributeDefinitionBuilder
    .of("priceMax", LocalizedStrings.of(Locale.ENGLISH, "priceMax"))
    .isRequired(false)
    .build()
}

//class DemandProductDraftSupplier(productTypeRef: Referenceable[ProductType], name: String)
//  extends Supplier[ProductDraft] {
//  val productType = productTypeRef.toReference
//
//  override def get(): ProductDraft = {
//    val masterVariant: ProductVariantDraft = ProductVariantDraftBuilder.of()
////      .plusAttribute(CardType.attribute.valueOf(CardType.demand))
//      .plusAttribute(AttributeAccess.ofString().getterSetter("userId"))
//      .build()
//    val slug: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))
//
//    ProductDraftBuilder.of(productType, LocalizedStrings.of(Locale.ENGLISH, name), slug, masterVariant).build()
//  }
//}

//object CardType {
//    val demand = PlainEnumValue.of("Demand", "Demand")
//
//    val values = PlainEnumValue.of("Demand", "Demand") :: PlainEnumValue.of("Offer", "Offer") :: Nil
//    val attribute = AttributeAccess.ofPlainEnumValue().getterSetter("cardType");
//}
package model.sphere

import java.util.Locale
import java.util.function.Supplier

import com.github.slugify.Slugify
import io.sphere.sdk.attributes.{AttributeAccess, EnumAttributeDefinitionBuilder, AttributeDefinition}
import io.sphere.sdk.models.{Referenceable, PlainEnumValue, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantDraft, ProductVariantDraftBuilder, ProductDraftBuilder, ProductDraft}
import io.sphere.sdk.producttypes.{ProductType, ProductTypeDraft}
import io.sphere.sdk.attributes._
import scala.collection.JavaConversions._

class DemandProductTypeDraftSupplier extends Supplier[ProductTypeDraft] {

  override def get = ProductTypeDraft.of("demand", "desc", createAttributes())

  def createAttributes(): java.util.List[AttributeDefinition] = userIdAttribute :: Nil

  def userIdAttribute = TextAttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  def distanceAttribute = NumberAttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"))
    .isRequired(true)
    .build()

  def priceMinAttribute = MoneyAttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"))
    .isRequired(true)
    .build()
}

class DemandProductDraftSupplier(productTypeRef: Referenceable[ProductType], name: String)
  extends Supplier[ProductDraft] {
  val productType = productTypeRef.toReference

  override def get(): ProductDraft = {
    val masterVariant: ProductVariantDraft = ProductVariantDraftBuilder.of()
//      .plusAttribute(CardType.attribute.valueOf(CardType.demand))
      .plusAttribute(AttributeAccess.ofString().getterSetter("userId").valueOf(???))
      .build()
    val slug: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))

    ProductDraftBuilder.of(productType, LocalizedStrings.of(Locale.ENGLISH, name), slug, masterVariant).build()
  }
}

//class CardProductTypeDraftSupplier extends Supplier[ProductTypeDraft] {
//
//  override def get = ProductTypeDraft.of("card", "desc", createAttributes())
//
//  def createAttributes(): java.util.List[AttributeDefinition] = createCardTypeAttribute :: Nil
//
//  def createCardTypeAttribute = {
//    val cardTypeAttributeLabel: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, "cardType").plus(Locale.GERMAN, "Kartentyp")
//
//    EnumAttributeDefinitionBuilder.of("cardType", cardTypeAttributeLabel, CardType.values).required(true).build()
//  }
//}

//object CardType {
//    val demand = PlainEnumValue.of("Demand", "Demand")
//
//    val values = PlainEnumValue.of("Demand", "Demand") :: PlainEnumValue.of("Offer", "Offer") :: Nil
//    val attribute = AttributeAccess.ofPlainEnumValue().getterSetter("cardType");
//}

//class OfferProductTypeDraftSupplier extends Supplier[ProductTypeDraft] {
//
//  override def get = ProductTypeDraft.of("offer", "desc", createAttributes())
//
//  def createAttributes(): java.util.List[AttributeDefinition] = createSampleAttribute :: Nil
//
//  def createSampleAttribute = BooleanAttributeDefinitionBuilder
//    .of("sample_name", LocalizedStrings.of(Locale.ENGLISH, "bla"))
//    .build()
//}

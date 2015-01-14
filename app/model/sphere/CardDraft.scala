package model.sphere

import java.util.Locale
import java.util.function.Supplier

import com.github.slugify.Slugify
import io.sphere.sdk.attributes.{AttributeAccess, EnumAttributeDefinitionBuilder, AttributeDefinition}
import io.sphere.sdk.models.{Referenceable, PlainEnumValue, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantDraft, ProductVariantDraftBuilder, ProductDraftBuilder, ProductDraft}
import io.sphere.sdk.producttypes.{ProductType, ProductTypeDraft}
import scala.collection.JavaConversions._


object CardType {
  val demand = PlainEnumValue.of("Demand", "Demand")
  val offer = PlainEnumValue.of("Offer", "Offer")

  val values = demand :: offer :: Nil
  val attribute = AttributeAccess.ofPlainEnumValue().getterSetter("cardType")
}

class CardProductTypeDraft extends Supplier[ProductTypeDraft] {

  override def get = ProductTypeDraft.of("card1", "desc", createAttributes())

  def createAttributes(): java.util.List[AttributeDefinition] = createCardTypeAttribute :: Nil

  def createCardTypeAttribute = {
    val cardTypeAttributeLabel: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, "cardType").plus(Locale.GERMAN, "Kartentyp")

    EnumAttributeDefinitionBuilder.of("cardType", cardTypeAttributeLabel, CardType.values).required(true).build()
  }
}

class DemandProductDraftSupplier(productType: Referenceable[ProductType], name: String) extends Supplier[ProductDraft] {
  override def get(): ProductDraft = {
    val masterVariant: ProductVariantDraft = ProductVariantDraftBuilder.of()
      .plusAttribute(CardType.attribute.valueOf(CardType.demand))
      .build()
    val slug: LocalizedStrings = LocalizedStrings.of(Locale.ENGLISH, new Slugify().slugify(name))

    ProductDraftBuilder.of(productType, LocalizedStrings.of(Locale.ENGLISH, name), slug, masterVariant).build()
  }
}

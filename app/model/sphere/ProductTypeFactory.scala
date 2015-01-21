package model.sphere

import java.util.Locale
import java.util.function.Supplier

import com.github.slugify.Slugify
import common.helper.Configloader
import io.sphere.sdk.attributes.{AttributeAccess, EnumAttributeDefinitionBuilder, AttributeDefinition}
import io.sphere.sdk.models.{Referenceable, PlainEnumValue, LocalizedStrings}
import io.sphere.sdk.products.{ProductVariantDraft, ProductVariantDraftBuilder, ProductDraftBuilder, ProductDraft}
import io.sphere.sdk.producttypes.{ProductType, ProductTypeDraft}
import io.sphere.sdk.attributes._
import scala.collection.JavaConversions._

object ProductTypeFactory {

  def demandType = ProductTypeDraft.of(Configloader.getStringOpt("demand.typeName").get, "desc", demandAttributes)
  def offerType = ProductTypeDraft.of(Configloader.getStringOpt("offer.typeName").get, "desc", offerAttributes)

  private val idAttribute = TextAttributeDefinitionBuilder
    .of("id", LocalizedStrings.of(Locale.ENGLISH, "id"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val userIdAttribute = TextAttributeDefinitionBuilder
    .of("userId", LocalizedStrings.of(Locale.ENGLISH, "userId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val productIdAttribute = TextAttributeDefinitionBuilder
    .of("productId", LocalizedStrings.of(Locale.ENGLISH, "productId"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val tagsAttribute = TextAttributeDefinitionBuilder
    .of("tags", LocalizedStrings.of(Locale.ENGLISH, "tags"), TextInputHint.SINGLE_LINE)
    .isRequired(true)
    .build()

  private val longAttribute = NumberAttributeDefinitionBuilder
    .of("longitude", LocalizedStrings.of(Locale.ENGLISH, "longitude"))
    .isRequired(true)
    .build()

  private val latAttribute = NumberAttributeDefinitionBuilder
    .of("latitude", LocalizedStrings.of(Locale.ENGLISH, "latitude"))
    .isRequired(true)
    .build()

  private val distanceAttribute = NumberAttributeDefinitionBuilder
    .of("distance", LocalizedStrings.of(Locale.ENGLISH, "distance"))
    .isRequired(false)
    .build()

  private val priceAttribute = MoneyAttributeDefinitionBuilder
    .of("price", LocalizedStrings.of(Locale.ENGLISH, "price"))
    .isRequired(false)
    .build()

  private val priceMinAttribute = MoneyAttributeDefinitionBuilder
    .of("priceMin", LocalizedStrings.of(Locale.ENGLISH, "priceMin"))
    .isRequired(false)
    .build()

  private val priceMaxAttribute = MoneyAttributeDefinitionBuilder
    .of("priceMax", LocalizedStrings.of(Locale.ENGLISH, "priceMax"))
    .isRequired(false)
    .build()

  private val demandAttributes: java.util.List[AttributeDefinition] =
    idAttribute ::
    userIdAttribute ::
    productIdAttribute ::
    tagsAttribute ::
    longAttribute ::
    latAttribute ::
    distanceAttribute ::
    priceMinAttribute ::
    priceMaxAttribute ::
    Nil

  private val offerAttributes: java.util.List[AttributeDefinition] =
    idAttribute ::
    userIdAttribute ::
    tagsAttribute ::
    longAttribute ::
    latAttribute ::
    priceAttribute ::
    Nil
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

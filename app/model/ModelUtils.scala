package model

import common.domain.ExternalImage
import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.ProductVariant
import scala.collection.JavaConverters._

trait ModelUtils {
  def getAttribute(variant: ProductVariant, name: String) =
    variant.getAttribute(name).get()

  def readStringAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofString().attributeMapper())

  def readDoubleAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofDouble().attributeMapper())

  def readMoneyAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofMoney().attributeMapper())

  def readImages(variant: ProductVariant) =
    variant.getImages.asScala.toList map {img => ExternalImage.fromSphereImage(img)}
}

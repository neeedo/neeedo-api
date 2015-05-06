package model

import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.{ProductVariant, Product}

trait ModelUtils {
  def getAttribute(variant: ProductVariant, name: String) =
    variant.getAttribute(name).get()

  def readStringAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofString().attributeMapper())

  def readDoubleAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofDouble().attributeMapper())

  def readMoneyAttribute(variant: ProductVariant, name: String) =
    getAttribute(variant, name).getValue(AttributeAccess.ofMoney().attributeMapper())
}

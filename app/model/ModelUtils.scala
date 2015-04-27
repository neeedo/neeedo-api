package model

import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.Product

trait ModelUtils {
  def getAttribute(product: Product, name: String) =
    product.getMasterData.getStaged.getMasterVariant.getAttribute(name).get()

  def readStringAttribute(name: String)(implicit product: Product) =
    getAttribute(product, name).getValue(AttributeAccess.ofString().attributeMapper())

  def readDoubleAttribute(name: String)(implicit product: Product) =
    getAttribute(product, name).getValue(AttributeAccess.ofDouble().attributeMapper())

  def readMoneyAttribute(name: String)(implicit product: Product) =
    getAttribute(product, name).getValue(AttributeAccess.ofMoney().attributeMapper())
}

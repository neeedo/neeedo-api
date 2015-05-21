package model

import io.sphere.sdk.attributes.AttributeAccess
import io.sphere.sdk.products.ProductVariant
import java.util
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

  def readStringSetAttribute(variant: ProductVariant, name: String) = {
    val s: util.Set[String] = getAttribute(variant, name).getValue(AttributeAccess.ofStringSet().attributeMapper())
    collection.immutable.Seq(s.asScala.toSeq: _*).toSet
  }

}

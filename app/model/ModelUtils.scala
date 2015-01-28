package model

import io.sphere.sdk.products.Product

trait ModelUtils {
  def getAttribute(product: Product, name: String) =
    product.getMasterData.getStaged.getMasterVariant.getAttribute(name).get()
}

package common.sphere

import common.domain.ExternalImage
import io.sphere.sdk.products.commands.updateactions.AddExternalImage
import io.sphere.sdk.products.{ProductUpdateScope, Product}
import io.sphere.sdk.products.commands.ProductUpdateCommand

object AddImageCommand {
  def apply(product: Product, image: ExternalImage): ProductUpdateCommand = {
    val sphereImage = ExternalImage.toSphereImage(image)
    val variantId = product.getMasterData.getStaged.getMasterVariant.getId
    val updateScope = ProductUpdateScope.STAGED_AND_CURRENT

    ProductUpdateCommand.of(product, AddExternalImage.of(sphereImage, variantId, updateScope))
  }
}

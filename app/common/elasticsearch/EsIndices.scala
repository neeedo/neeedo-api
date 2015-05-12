package common.elasticsearch

import common.domain.IndexName
import common.helper.Configloader

object EsIndices {
  val demandIndexName = IndexName(Configloader.getString("demand.typeName"))
  val offerIndexName = IndexName(Configloader.getString("offer.typeName"))
  val demandTypeName = demandIndexName.toTypeName
  val offerTypeName = offerIndexName.toTypeName
}

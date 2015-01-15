package common.domain

import model.DemandId

sealed trait AddDemandResult
object DemandSaveFailed extends AddDemandResult
case class DemandSaved(id: DemandId) extends AddDemandResult
object DemandSaveSphereFailed extends AddDemandResult
object DemandSaveEsFailed extends AddDemandResult


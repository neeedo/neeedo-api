package common.domain

sealed trait AddDemandResult
object DemandSaveFailed extends AddDemandResult
object DemandSaved extends AddDemandResult


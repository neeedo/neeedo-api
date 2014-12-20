package common.domain

sealed trait AddDemandResult
object DemandCouldNotBeSaved extends AddDemandResult
object DemandSaved extends AddDemandResult



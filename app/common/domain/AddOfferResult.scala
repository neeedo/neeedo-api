package common.domain

sealed trait AddOfferResult
object OfferSaveFailed extends AddOfferResult
object OfferSaved extends AddOfferResult



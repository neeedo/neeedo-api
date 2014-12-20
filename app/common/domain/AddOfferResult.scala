package common.domain

sealed trait AddOfferResult
object OfferCouldNotBeSaved extends AddOfferResult
object OfferSaved extends AddOfferResult



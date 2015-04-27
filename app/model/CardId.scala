package model

import play.api.mvc.PathBindable

sealed trait CardId { val value: String }


case class DemandId(value: String) extends CardId

object DemandId {
  implicit def pathBindable: PathBindable[DemandId] = new PathBindable[DemandId] {
    override def bind(key: String, value: String): Either[String, DemandId] = {
      DemandId(value) match {
        case demandId: DemandId => Right(demandId)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, demandId: DemandId): String = demandId.value
  }
}


case class OfferId(value: String) extends CardId

object OfferId {
  implicit def pathBindable: PathBindable[OfferId] = new PathBindable[OfferId] {
    override def bind(key: String, value: String): Either[String, OfferId] = {
      OfferId(value) match {
        case offerId: OfferId => Right(offerId)
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, offerId: OfferId): String = offerId.value
  }
}

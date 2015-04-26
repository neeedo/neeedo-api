package model

import play.api.mvc.PathBindable

sealed trait CardId {
  val value: String
}


case class DemandId(value: String) extends CardId

object DemandId {
  implicit def pathBinder: PathBindable[DemandId] = new PathBindable[DemandId] {

    override def bind(key: String, value: String): Either[String, DemandId] = {
      DemandId.apply(value) match {
        case x: DemandId => Right(x)
        //TODO id validation here!
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, demandId: DemandId): String = demandId.value
  }
}


case class OfferId(value: String) extends CardId

object OfferId {
  implicit def pathBinder: PathBindable[OfferId] = new PathBindable[OfferId] {

    override def bind(key: String, value: String): Either[String, OfferId] = {
      OfferId.apply(value) match {
        case x: OfferId => Right(x)
        //TODO id validation here!
        case _	=> Left("Bla")
      }
    }

    override def unbind(key: String, offerId: OfferId): String = offerId.value
  }
}

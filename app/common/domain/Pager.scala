package common.domain

import play.api.mvc.QueryStringBindable

case class Pager(limit: Int, offset: Int)

object Pager {
  implicit def queryStringBinder(implicit intBinder: QueryStringBindable[Int]) = new QueryStringBindable[Pager] {
    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Pager]] = {
      for {
        limit <- Some(intBinder.bind("limit", params).getOrElse(Right(20)))
        offset <- Some(intBinder.bind("offset", params).getOrElse(Right(0)))
      } yield {
        (limit, offset) match {
          case (Right(l), Right(o)) => Right(Pager(l, o))
          case _ => Left("Unable to bind a Pager")
        }
      }
    }
    override def unbind(key: String, pager: Pager): String = {
      intBinder.unbind("limit", pager.limit) + "&" + intBinder.unbind("offset", pager.offset)
    }
  }
}
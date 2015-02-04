package common.domain

import play.api.mvc.PathBindable

case class PageSize(value: Int) extends AnyVal
object PageSize {
  implicit def pathBinder: PathBindable[PageSize] = new PathBindable[PageSize] {
    override def bind(key: String, value: String): Either[String, PageSize] = {
      PageSize.apply(value.toInt) match {
        case x: PageSize => Right(x)
        //TODO id validation here!
        case _	=> Left("Bla")
      }
    }
    override def unbind(key: String, pageSize: PageSize): String = pageSize.value.toString
  }
}

case class From(value: Int) extends AnyVal
object From {
  implicit def pathBinder: PathBindable[From] = new PathBindable[From] {
    override def bind(key: String, value: String): Either[String, From] = {
      From.apply(value.toInt) match {
        case x: From => Right(x)
        //TODO id validation here!
        case _	=> Left("Bla")
      }
    }
    override def unbind(key: String, from: From): String = from.value.toString
  }
}

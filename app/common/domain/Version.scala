package common.domain

import play.api.mvc.PathBindable

case class Version(value: Long) {
  def ++ = Version(this.value + 1)
}

object Version {
  implicit def pathBinder: PathBindable[Version] = new PathBindable[Version] {
    override def bind(key: String, value: String): Either[String, Version] = {
      Version.apply(value.toLong) match {
        case x: Version => Right(x)
        //TODO version validation here!
        case _	=> Left("Bla")
      }
    }
    override def unbind(key: String, version: Version): String = version.value.toString
  }
}


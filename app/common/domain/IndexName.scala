package common.domain

case class IndexName(value: String) extends AnyVal {
  def toTypeName = TypeName(value)
}

case class TypeName(value: String) extends AnyVal {
  def toIndexName = IndexName(value)
}

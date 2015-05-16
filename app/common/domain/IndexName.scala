package common.domain

case class IndexName(value: String) {
  def toTypeName = TypeName(value)
}

case class TypeName(value: String) {
  def toIndexName = IndexName(value)
}

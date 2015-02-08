package common.elasticsearch

import common.domain.TypeName
import common.helper.FileHelper

case class EsMapping(name: TypeName, file: String) {
  def value: String = FileHelper.stringFromFile(file)
}

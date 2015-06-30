package common.elasticsearch

import common.domain.TypeName
import common.helper.FileHelper

case class EsMapping(name: TypeName, file: String) {
  def value: String = FileHelper.stringFromFile(file)
}

case class EsSettings(file: String) {
  def value: String = FileHelper.stringFromFile(file)
}

object StandardSettings extends EsSettings("migrations/standard-settings.json")
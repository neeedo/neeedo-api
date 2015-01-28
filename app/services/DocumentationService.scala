package services

import scala.io.Source

class DocumentationService {
  
  def readDocumentation(): String = {
    Source.fromFile("api.wiki/APIDoc.md").mkString
  }
}

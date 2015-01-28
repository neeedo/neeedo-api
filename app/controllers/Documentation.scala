package controllers

import play.api.mvc._
import services.DocumentationService

class Documentation(service: DocumentationService) extends Controller {

  def showDocumentation = Action {
    Ok(service.readDocumentation())
  }
}

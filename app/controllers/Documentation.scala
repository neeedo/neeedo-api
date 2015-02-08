package controllers

import play.api.mvc._

class Documentation extends Controller {

  def showDocumentation = Action {
//    TODO get readDocumentation() working again
//    Ok(service.readDocumentation())
    Redirect("https://github.com/HTW-Projekt-2014-Commercetools/api/blob/master/README.md")
  }
}

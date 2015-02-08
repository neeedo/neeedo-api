package controllers

import play.api.mvc._

class Documentation extends Controller {

  def showDocumentation = Action {
//    // currently not working correct, do we need this anyways?
//    Ok(service.readDocumentation())
    // TODO get readDocumentation() working again -> if we do a redirect here, cross origin request won't work!
//    Ok
    Redirect("https://github.com/HTW-Projekt-2014-Commercetools/api/blob/master/README.md")
  }
}

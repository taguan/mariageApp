package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    val template = views.html.index("my app is so beautiful")
    Ok(template)
  }

}
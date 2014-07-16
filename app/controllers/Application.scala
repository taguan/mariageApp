package controllers

import com.google.inject.Singleton
import play.api._
import play.api.mvc._

@Singleton
class Application extends Controller {

  def index = Action {
    Ok(views.html.main())
  }

}
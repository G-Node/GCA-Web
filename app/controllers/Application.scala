package controllers

import play.api._
import play.api.mvc._

object Application extends Controller with securesocial.core.SecureSocial {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def showUserInfo = UserAwareAction { implicit request =>
    val userName = request.user match {
      case Some(user) => user.fullName
      case _ => "guest"
    }

    Ok("Hello %s".format(userName))
  }

  def showSecret = SecuredAction { implicit request =>
    Ok("The answer is 42.")
  }

}
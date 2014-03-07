package controllers

import play.api.mvc._
import models.{Conference, Account, Abstract}

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

  def submission = UserAwareAction { implicit request => // TOTO should be a secure action
    val user: Account = request.user match {
      case Some(user: Account) => user
      case _                   => null
    }
    val conf = Conference(None, Option("BCCN14"))
    Ok(views.html.submission(user, conf, new Abstract))
  }

}

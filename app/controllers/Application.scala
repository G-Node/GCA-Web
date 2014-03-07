package controllers

import play.api._
import play.api.mvc._
import utils.GCAAuth
import models.{Conference, Account, Abstract}

object Application extends Controller with GCAAuth {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def showUserInfo = AccountAwareAction { implicit request =>
    val userName = request.user match {
      case Some(account: Account) => account.fullName + " [" + account.firstName + " " + account.lastName + "]"
      case _ => "guest"
    }

    Logger.debug(request.uri)
    Logger.debug(request.domain)
    Logger.debug(request.host)
    Ok("Hello %s".format(userName))
  }

  def showSecret = AuthenticatedAction { implicit request =>
    Logger.debug(request.user.toString)

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

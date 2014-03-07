package controllers

import play.api._
import play.api.mvc._
import models.Account
import utils.GCAAuth

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

}
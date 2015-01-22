package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import models.Login
import play.api.mvc.Action

/**
 * Controller serving login and sign up pages.
 */
class Accounts(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  def logIn = UserAwareAction { implicit request =>
    Ok("log in form");
  }

  def signUp = UserAwareAction { implicit request =>
    Ok("sign up form");
  }

  def logOut = SecuredAction { implicit request =>
    Ok("logout");
  }

  def notAuthenticated = Action { implicit request =>
    Ok("not authenticated");
  }

  def create = Action { implicit request =>
    Ok("account created");
  }

}

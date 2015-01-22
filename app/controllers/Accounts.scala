package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import forms.SignInForm
import models._
import play.api.mvc.Action

/**
 * Controller serving login and sign up pages.
 */
class Accounts(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  def logIn = UserAwareAction { implicit request =>

    request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case _ => Ok(views.html.login(SignInForm.form))
    }
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

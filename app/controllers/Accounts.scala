package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.Silhouette
import conf.GlobalEnvironment
import forms.{SignInForm, SignUpForm}
import models._
import play.api.mvc.Action
import service.AccountStore

/**
 * Controller serving login and sign up pages.
 */
class Accounts(implicit val env: GlobalEnvironment)
  extends Silhouette[Login, CachedCookieAuthenticator] {

  val accountService = new AccountStore(env.pwHasher)

  def logIn = UserAwareAction { implicit request =>

    request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case _ => Ok(views.html.login(SignInForm.form))
    }
  }

  def signUp = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case _ => Ok(views.html.signup(SignUpForm.form))
    }
  }

  def logOut = SecuredAction { implicit request =>
    val result = Redirect(routes.Application.index)
    env.authenticatorService.discard(result);
  }

  def notAuthenticated = Action { implicit request =>
    Ok("not authenticated");
  }

  def create = Action { implicit request =>
    SignUpForm.form.bindFromRequest.fold (
      invalid => BadRequest(views.html.signup(invalid)),
      ok => {
        try {
          accountService.create(Account(null, ok.email, ok.firstName, ok.lastName, None), Some(ok.password))
          Redirect(routes.Application.conferences()) // TODO success message
        } catch {
          case e: Throwable => Redirect(routes.Accounts.signUp()).flashing("error" -> e.getMessage)
        }
      }
    )
  }

}

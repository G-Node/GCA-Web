package controllers

import javax.persistence.NoResultException

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{LoginInfo, Silhouette}
import conf.GlobalEnvironment
import forms.{ResetPasswordForm, SignInForm, SignUpForm}
import models._
import play.api.mvc.Action
import service.{AccountStore, CredentialsStore}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

/**
 * Controller serving login and sign up pages.
 */
class Accounts(implicit val env: GlobalEnvironment)
  extends Silhouette[Login, CachedCookieAuthenticator] {

  val accountService = new AccountStore(env.pwHasher)
  val credentialStore = new CredentialsStore()
  
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

  def passwordResetPage = SecuredAction { implicit request =>
    Ok(views.html.passwordreset(ResetPasswordForm.passwordsForm))
  }

  def passwordResetCommit = SecuredAction { implicit request =>
    ResetPasswordForm.passwordsForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.passwordreset(formWithErrors)),
      passwords => {
        val loginInfo = LoginInfo(env.credentialsProvider.id, request.identity.account.mail)
        val pwInfo = env.pwHasher.hash(passwords._1)

        Await.result(env.authInfoService.save(loginInfo, pwInfo), 5 seconds)
        Redirect(routes.Application.index()).flashing("success" -> "Password successfully changed")
      }
    )
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

  def activate(token: String) = Action.async { implicit request =>
    credentialStore.activate(token).map { pwInfo =>
      Redirect(routes.Accounts.logIn()).flashing("info" -> "You successfully activated your account")
    }.recover {
      case ex : NoResultException =>
        NotFound(views.html.error.NotFound())
    }
  }

}

package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.contrib.utils.BCryptPasswordHasher
import com.mohiva.play.silhouette.core.{LoginInfo, Silhouette}
import conf.GlobalEnvironment
import forms.{ResetPasswordForm, SignInForm, SignUpForm}
import models._
import play.api.mvc.Action
import service.AccountStore

import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import scala.util.Success


/**
 * Controller serving login and sign up pages.
 */
class Accounts(implicit val env: GlobalEnvironment)
  extends Silhouette[Login, CachedCookieAuthenticator] {

  val accountService = new AccountStore()
  val pwHasher = new BCryptPasswordHasher()

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
    SignUpForm.form.fold(
      invalid => {
        BadRequest(views.html.signup(invalid))
      },
      ok => {
        try {
          val account = accountService.create(Account(null, ok.email, ok.firstName, ok.lastName, None))

          val loginInfo = LoginInfo(env.credentialsProvider.id, account.mail)
          val pwInfo = env.pwHasher.hash(ok.password)

          val pwStored = Await.result(env.authInfoService.save(loginInfo, pwInfo), Duration.Inf)

          Ok("fixme")
        } catch {
          case e: Throwable => Redirect(routes.Accounts.signUp()).flashing("error" -> e.getMessage)
        }
      }
    )
  }

}

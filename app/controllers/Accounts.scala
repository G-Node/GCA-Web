package controllers

import java.util.UUID
import javax.persistence.NoResultException

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{LoginInfo, Silhouette}
import conf.GlobalEnvironment
import forms.{EmailForm, ResetPasswordForm, SignInForm, SignUpForm}
import models._
import play.api.mvc.Action
import service.mail.MailerService
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
  val mailer = new MailerService
  val credentialStore = new CredentialsStore()
  
  def logIn = UserAwareAction { implicit request =>

    request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case _ => Ok(views.html.login(SignInForm.form))
    }
  }

  def logOut = SecuredAction { implicit request =>
    val result = Redirect(routes.Application.index)
    env.authenticatorService.discard(result);
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

  def forgotPasswordPage = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.Application.index())
      case None => Ok(views.html.forgotpassword(EmailForm.emailForm))
    }
  }

  def forgotPasswordCommit = Action { implicit request =>
    EmailForm.emailForm.bindFromRequest.fold (
      formWithErrors => Ok(views.html.forgotpassword(formWithErrors)),
      email => {
        val account = accountService.getByMail(email)

        val newpass = UUID.randomUUID.toString.substring(0, 8);
        val loginInfo = LoginInfo(env.credentialsProvider.id, account.mail)
        val pwInfo = env.pwHasher.hash(newpass)

        mailer.sendPasswordReset(account, newpass, routes.Accounts.logIn().absoluteURL())

        Await.result(env.authInfoService.save(loginInfo, pwInfo), 5 seconds)

        Redirect(routes.Accounts.logIn()).flashing("success" -> "Password reset and sent to you by email")
      }
    )
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

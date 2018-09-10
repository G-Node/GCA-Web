package controllers

import java.util.UUID
import javax.persistence.NoResultException

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.providers.PasswordInfo
import com.mohiva.play.silhouette.core.{LoginInfo, Silhouette}
import conf.GlobalEnvironment
import forms._
import models._
import play.api.http.Status._
import play.api.mvc.Action
import service.mail.MailerService
import service.{AccountStore, CredentialsStore}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.reflect._

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
  
  def emailchange = SecuredAction { implicit request =>
    Ok(views.html.changemail(NewMailForm.newmailform))
  }

  def emailchangeCommit = SecuredAction { implicit request =>
    NewMailForm.newmailform.bindFromRequest.fold(
      formWithErrors => Redirect(routes.Accounts.emailchange()).flashing("error" -> "Could not change email address"),
      nmailform => {
        var loginInfo = LoginInfo(env.credentialsProvider.id, request.identity.account.mail)
        val f = Await.result(env.authInfoService.retrieve(loginInfo)(classTag[PasswordInfo]), 10 seconds)
        val pwInfo = f.get
        // lets see whether password is corect. if not fail silently
        if (!env.pwHasher.matches(pwInfo, nmailform._3)) {
          Redirect(routes.Accounts.emailchange()).flashing("error" -> "Could not change email address")
        } else {
          // lets see whether mail exists. if so fail silently
          Await.result(env.authInfoService.retrieve(LoginInfo(env.credentialsProvider.id, providerKey = nmailform._1))
          (classTag[PasswordInfo]), 10 seconds) match {
            case Some(x) =>
              Redirect(routes.Accounts.emailchange()).flashing("error" -> "Could not change email address")
            case _ =>
              Await.result(credentialStore.update(LoginInfo(env.credentialsProvider.id, providerKey = nmailform._1), loginInfo,
                pwInfo), 5 seconds)
              Redirect(routes.Application.index()).flashing("success" ->
                """Email successfully changed.""")
          }
        }
      }
    )
  }
  
  def passwordResetPage = SecuredAction { implicit request =>
    Ok(views.html.passwordreset(ResetPasswordForm.passwordsForm))
  }

  def passwordResetCommit = SecuredAction { implicit request =>
    ResetPasswordForm.passwordsForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.passwordreset(formWithErrors)),
      passwords => {
        var loginInfo = LoginInfo(env.credentialsProvider.id, request.identity.account.mail)
        val f = Await.result(env.authInfoService.retrieve(loginInfo)(classTag[PasswordInfo]), 10 seconds)
        val pwInfo = f.get
        // lets see whether old password is corect. if not fail silently
        if (!env.pwHasher.matches(pwInfo, passwords._1)) {
          Redirect(routes.Accounts.emailchange()).flashing("error" -> "Could not change email address")
        }else {
          val newPwInfo = env.pwHasher.hash(passwords._2)
          Await.result(env.authInfoService.save(loginInfo, newPwInfo), 5 seconds)
          Redirect(routes.Application.index()).flashing("success" ->
            """Password successfully changed.
          If you don't get a confirmation email in a few moments, Please check your spam folder.""")
        }
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
        try {
          val account = accountService.getByMail(email)

          val newpass = UUID.randomUUID.toString.substring(0, 8)
          val loginInfo = LoginInfo(env.credentialsProvider.id, account.mail)
          val pwInfo = env.pwHasher.hash(newpass)

          mailer.sendPasswordReset(account, newpass, routes.Accounts.logIn().absoluteURL())

          Await.result(env.authInfoService.save(loginInfo, pwInfo), 5 seconds)

          Redirect(routes.Accounts.logIn()).flashing("success" ->
            """Password was reset and sent to you by email.
            If you don't get it in a few moments, please check your spam folder.""")

        } catch {
          case e: NoResultException => Redirect(routes.Accounts.forgotPasswordPage())
            .flashing("error" -> "Account with this email does not exist.")
        }

      }
    )
  }

  def notAuthenticated = Action { implicit request =>
    Ok("not authenticated");
  }

  def create = Action { implicit request =>
    val flashing = "Your account was created. An email with " +
      "a link to activate the account was sent to your email " +
      "address. Please check your spam folder if the message " +
      "is not delivered within a few minutes."

    SignUpForm.form.bindFromRequest.fold (
      invalid => BadRequest(views.html.signup(invalid)),
      ok => {
        try {
          accountService.create(Account(null, ok.email, ok.firstName, ok.lastName, None), Some(ok.password))
          Redirect(routes.Application.conferences()).flashing("info" -> flashing)
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

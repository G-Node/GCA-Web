package views.auth

import play.api.mvc.{RequestHeader, Request}
import play.api.templates.{Html, Txt}
import play.api.Application
import securesocial.core.{Identity, SecuredRequest}
import play.api.data.Form
import securesocial.controllers.Registration.RegistrationInfo
import securesocial.controllers.PasswordChange.ChangeInfo
import securesocial.controllers.TemplatesPlugin


class AuthViewsPlugin(application: Application) extends TemplatesPlugin {
  override def getLoginPage[A](implicit request: Request[A], form: Form[(String, String)],
                               msg: Option[String] = None): Html =
  {
    //securesocial.views.html.login(form, msg)
    views.html.auth.login(form, msg)
  }

  override def getSignUpPage[A](implicit request: Request[A], form: Form[RegistrationInfo], token: String): Html = {
    views.html.auth.registerDetails(form, token)
  }

  override def getStartSignUpPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.auth.register(form)
  }

  override def getStartResetPasswordPage[A](implicit request: Request[A], form: Form[String]): Html = {
    views.html.auth.resetPasswordStart(form)
  }

  def getResetPasswordPage[A](implicit request: Request[A], form: Form[(String, String)], token: String): Html = {
    views.html.auth.resetPasswordFinish(form, token)
  }

  def getPasswordChangePage[A](implicit request: SecuredRequest[A], form: Form[ChangeInfo]):Html = {
    views.html.auth.changePassword(form)
  }

  def getNotAuthorizedPage[A](implicit request: Request[A]): Html = {
    views.html.error.NotAuthorized()
  }

  def getSignUpEmail(token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.signUpEmail(token)))
  }

  def getAlreadyRegisteredEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.alreadyRegisteredEmail(user)))
  }

  def getWelcomeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.welcomeEmail(user)))
  }

  def getUnknownEmailNotice()(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.unknownEmailNotice(request)))
  }

  def getSendPasswordResetEmail(user: Identity, token: String)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordResetEmail(user, token)))
  }

  def getPasswordChangedNoticeEmail(user: Identity)(implicit request: RequestHeader): (Option[Txt], Option[Html]) = {
    (None, Some(securesocial.views.html.mails.passwordChangedNotice(user)))
  }
}
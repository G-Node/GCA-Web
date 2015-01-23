package controllers

import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import service.LoginStore

import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.providers.CredentialsProvider
import com.mohiva.play.silhouette.core.{LoginEvent, Silhouette, Environment}
import forms.SignInForm
import models.Login
import play.api.mvc.Action

/**
 * Controller that performs authentication for different providers.
 */
class Authentication(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  /**
   * Authenticate an account with a giver provider.
   *
   * @param provider The provider name.
   */
  def authenticate(provider: String) = Action.async { implicit request =>

    (env.providers.get(provider) match {
      case Some(p: CredentialsProvider) => SignInForm.form.bindFromRequest().fold(
        err => Future.failed(throw new AuthenticationException("Invalid form data")),
        data => p.authenticate(data))
      case _ => Future.failed(new AuthenticationException("Unsupported provider"))
    }).flatMap { loginInfo =>
      val result = Redirect(routes.Application.index)

      val loginStore = new LoginStore() //FIXME
      loginStore.retrieve(loginInfo).flatMap {
        case Some(user) => env.authenticatorService.create(user).map {
          case Some(authenticator) =>
            env.eventBus.publish(LoginEvent(user, request, request2lang))
            env.authenticatorService.send(authenticator, result)
          case None => throw new AuthenticationException("Authenticator error")
        }
        case None => Future.failed(new AuthenticationException("Invalid user"))
      }.recoverWith(exceptionHandler)
    }
  }

}

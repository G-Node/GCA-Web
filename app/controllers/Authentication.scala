package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
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
  def authenticate(provider: String) = Action { implicit request =>
    Ok("authenticate with provider: " + provider);
  }

}

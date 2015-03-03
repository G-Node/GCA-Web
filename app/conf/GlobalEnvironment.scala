package conf

import com.mohiva.play.silhouette.contrib.services.{CachedCookieAuthenticator, CachedCookieAuthenticatorService, CachedCookieAuthenticatorSettings, DelegableAuthInfoService}
import com.mohiva.play.silhouette.contrib.utils.{BCryptPasswordHasher, PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.core.providers.CredentialsProvider
import com.mohiva.play.silhouette.core.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.core.utils.Clock
import com.mohiva.play.silhouette.core.{Environment, EventBus, Provider}
import models.Login
import play.api.{Application, Play}
import service.{CredentialsStore, LoginStore}

/**
 * Global environment implementation
 */
class GlobalEnvironment(implicit val app: Application)  extends Environment[Login, CachedCookieAuthenticator] {

  lazy val idGenerator = new SecureRandomIDGenerator()

  lazy val cacheLayer = new PlayCacheLayer()

  lazy val authInfoService = new DelegableAuthInfoService(new CredentialsStore())

  lazy val pwHasher = new BCryptPasswordHasher()

  lazy val credentialsProvider = new CredentialsProvider(authInfoService, pwHasher, Seq(pwHasher))

  override lazy val identityService: IdentityService[Login] = new LoginStore

  override lazy val authenticatorService: AuthenticatorService[CachedCookieAuthenticator] = {
    new CachedCookieAuthenticatorService(CachedCookieAuthenticatorSettings(
      cookieName = Play.configuration.getString("silhouette.authenticator.cookieName").getOrElse("id"),
      cookiePath = Play.configuration.getString("silhouette.authenticator.cookiePath").getOrElse("/"),
      cookieDomain = Play.configuration.getString("silhouette.authenticator.cookieDomain"),
      secureCookie = Play.configuration.getBoolean("silhouette.authenticator.secureCookie").getOrElse(false),
      httpOnlyCookie = Play.configuration.getBoolean("silhouette.authenticator.httpOnlyCookie").getOrElse(true),
      cookieIdleTimeout = Play.configuration.getInt("silhouette.authenticator.cookieIdleTimeout").getOrElse(1800),
      cookieAbsoluteTimeout = Play.configuration.getInt("silhouette.authenticator.cookieAbsoluteTimeout"),
      authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").getOrElse(43200)
    ), cacheLayer, idGenerator, Clock())
  }

  override lazy val providers: Map[String, Provider] = Map(credentialsProvider.id -> credentialsProvider)
  override lazy val eventBus: EventBus = new EventBus
}
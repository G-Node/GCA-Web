package conf

import java.lang.reflect.Constructor
import javax.persistence.{EntityNotFoundException, NoResultException}

import com.mohiva.play.silhouette.contrib.services.{CachedCookieAuthenticatorSettings, CachedCookieAuthenticatorService, CachedCookieAuthenticator}
import com.mohiva.play.silhouette.contrib.utils.{PlayCacheLayer, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.core.{SecuredSettings, EventBus, Provider, Environment}
import com.mohiva.play.silhouette.core.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.core.utils.Clock
import models.Login
import play.api._
import play.api.libs.json.{JsError, JsResultException, Json}
import play.api.mvc.Results._
import play.api.mvc._
import service.LoginStore

import scala.concurrent.Future

object Global extends GlobalSettings with SecuredSettings {

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.error.NotFound()))
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[SimpleResult] = {
    Future.successful {
      if (acceptsJson(request))
        exHandlerJSON(ex)
      else
        exHandlerHTML(ex)
    }
  }

  def acceptsJson(request: RequestHeader) : Boolean = {
    request.accepts("application/json") || request.accepts("text/json")
  }

  def exHandlerHTML(e: Throwable) : SimpleResult = {
    e match {
      case e: NoResultException => NotFound(views.html.error.NotFound())
      case e: Exception => InternalServerError(views.html.error.InternalServerError(e))
    }
  }

  def exHandlerJSON(e: Throwable) : SimpleResult = {
    e match {
      case e: NoResultException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
      case e: EntityNotFoundException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
      case e: IllegalArgumentException => BadRequest(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
      case e: JsResultException => BadRequest(Json.obj("error" -> true, "causes" -> JsError.toFlatJson(e.errors)))
      case e: IllegalAccessException => Forbidden(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
      case e: Exception => InternalServerError(Json.obj("error" -> true, e.getMessage -> e.getStackTraceString))
    }
  }

  import play.api.Play.current

  lazy val idGenerator = new SecureRandomIDGenerator()
  lazy val cacheLayer = new PlayCacheLayer

  object GlobalEnv extends Environment[Login, CachedCookieAuthenticator] {
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

    override lazy val providers: Map[String, Provider] = Map[String, Provider]()
    override lazy val eventBus: EventBus = new EventBus
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && params(0) == classOf[Environment[Login, CachedCookieAuthenticator]]
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(GlobalEnv)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
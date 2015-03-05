package conf

import java.lang.reflect.Constructor
import javax.persistence.{EntityNotFoundException, NoResultException}

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.exceptions.AccessDeniedException
import com.mohiva.play.silhouette.core.{Environment, SecuredSettings}
import models.Login
import play.api._
import play.api.libs.json.{JsError, JsResultException, Json}
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object Global extends GlobalSettings with SecuredSettings {

  import play.api.Play.current
  implicit lazy val globalEnv = new GlobalEnvironment()

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.error.NotFound()))
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = {
    Logger.error(s"Error occurred on ${request.path}", ex)
    Future.successful {
      if (returnAsJson(request))
        exHandlerJSON(request, ex)
      else
        exHandlerHTML(request, ex)
    }
  }

  def returnAsJson(request: RequestHeader) : Boolean = {
    // Browsers send */* in the accept header therefore the path is
    // probably the best way for us to determine whether to serve json.
    // Any better idea is very wellcome.
    request.path.startsWith("/api/")
  }

  def exHandlerHTML(request: RequestHeader, ex: Throwable) : Result = {
    ex match {
      case e: NoResultException => NotFound(views.html.error.NotFound())
      case e: Exception => {
        e.getCause match {
          case cause: AccessDeniedException => Unauthorized(views.html.error.NotAuthorized())
          case _ => InternalServerError(views.html.error.InternalServerError(e))
        }
      }
    }
  }

  def exHandlerJSON(request: RequestHeader, ex: Throwable) : Result = {
    ex match {
      case e: NoResultException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTrace.toString))
      case e: EntityNotFoundException => NotFound(Json.obj("error" -> true, e.getMessage -> e.getStackTrace.toString))
      case e: IllegalArgumentException => BadRequest(Json.obj("error" -> true, e.getMessage -> e.getStackTrace.toString))
      case e: JsResultException => BadRequest(Json.obj("error" -> true, "causes" -> JsError.toFlatJson(e.errors)))
      case e: IllegalAccessException => Forbidden(Json.obj("error" -> true, e.getMessage -> e.getStackTrace.toString))
      case e: Exception => InternalServerError(Json.obj("error" -> true, e.getMessage -> e.getStackTrace.toString))
    }
  }

  override def getControllerInstance[A](controllerClass: Class[A]): A = {
    val instance = controllerClass.getConstructors.find { c =>
      val params = c.getParameterTypes
      params.length == 1 && classOf[Environment[Login, CachedCookieAuthenticator]].isAssignableFrom(params(0))
    }.map {
      _.asInstanceOf[Constructor[A]].newInstance(globalEnv)
    }
    instance.getOrElse(super.getControllerInstance(controllerClass))
  }
}
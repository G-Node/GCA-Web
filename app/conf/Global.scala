package conf

import javax.persistence.{EntityNotFoundException, NoResultException}

import play.api._
import play.api.libs.json.{JsError, JsResultException, Json}
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

object Global extends GlobalSettings {

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
}
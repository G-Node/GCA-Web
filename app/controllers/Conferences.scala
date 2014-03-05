package controllers

import play.api.Play.current
import play.api.mvc._
import play.api.libs.json._
import models.Account
import utils.serializer.ConferenceFormat
import utils.RESTResults._
import service.ConferenceService
import javax.persistence.{EntityNotFoundException, NoResultException}


/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
object Conferences extends Controller with OwnerManager with securesocial.core.SecureSocial {

  val httpPrefix = current.configuration.getString("httpPrefix").get

  /**
   * Create a new conference.
   *
   * @return Created with conference in JSON / BadRequest
   */
  def create = SecuredAction(parse.json) { request =>
    val user: Account = request.user match {
      case user: Account => user
      case _             => throw new ClassCastException
    }
    val formatter = new ConferenceFormat(httpPrefix + request.host)
    formatter.reads(request.body).fold(
      invalid = e => JSONValidationError(e),
      valid = conference => {
        try {
          val resp = ConferenceService().create(conference, user)
          Created(formatter.writes(resp))
        } catch {
          case e1: EntityNotFoundException => UserNotFound
          case e2: IllegalArgumentException => ObjectNotFound
        }
      }
    )
  }

  /**
   * List all available conferences.
   *
   * @return Ok with all conferences publicly available.
   */
  def list = Action { request =>
    val formatter = new ConferenceFormat(httpPrefix + request.host)
    Ok(JsArray(ConferenceService().list().map(formatter.writes(_))))
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   *
   * @return OK with conference in JSON / NotFound
   */
  def get(id: String) = Action { request =>
    val formatter = new ConferenceFormat(httpPrefix + request.host)
    try {
      Ok(formatter.writes(ConferenceService().get(id)))
    } catch {
      case e: NoResultException => ObjectNotFound
    }
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   *
   * @return OK with conference in JSON / BadRequest / Forbidden
   */
  def update(id: String) = SecuredAction(parse.json) { request =>
    val user: Account = request.user match {
      case user: Account => user
      case _             => throw new ClassCastException
    }
    val formatter = new ConferenceFormat(httpPrefix + request.host)
    formatter.reads(request.body).fold(
      invalid = e => JSONValidationError(e),
      valid = conference => {
        try {
          conference.uuid = id
          val resp = ConferenceService().update(conference, user)
          Ok(formatter.writes(resp))
        } catch {
          case e1: EntityNotFoundException => UserNotFound
          case e2: IllegalArgumentException => ObjectNotFound
          case e3: IllegalAccessException => AccessForbidden
        }
      }
    )
  }

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   *
   * @return OK | BadRequest | Forbidden
   */
  def delete(id: String) = SecuredAction { request =>
    val user: Account = request.user match {
      case user: Account => user
      case _             => throw new ClassCastException
    }
    try{
      ConferenceService().delete(id, user)
      Deleted
    } catch {
      case e1: EntityNotFoundException => UserNotFound
      case e2: IllegalArgumentException => ObjectNotFound
      case e3: IllegalAccessException => AccessForbidden
    }
  }

}


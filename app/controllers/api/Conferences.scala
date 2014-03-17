package controllers.api

import play.api.mvc._
import play.api.libs.json._
import utils.GCAAuth
import utils.serializer.ConferenceFormat
import service.ConferenceService
import utils.DefaultRoutesResolver._
import models.Conference

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
object Conferences extends Controller with OwnerManager with GCAAuth {

  implicit val confFormat = new ConferenceFormat()

  /**
   * Create a new conference.
   *
   * @return Created with conference in JSON / BadRequest
   */
  def create = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    val conference = request.body.as[Conference]
    val resp = ConferenceService().create(conference, request.user)

    Created(confFormat.writes(resp))
  }

  /**
   * List all available conferences.
   *
   * @return Ok with all conferences publicly available.
   */
  def list = Action { request =>
    Ok(JsArray(
      for (conf <- ConferenceService().list()) yield confFormat.writes(conf)
    ))
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   *
   * @return OK with conference in JSON / NotFound
   */
  def get(id: String) = Action { request =>
    Ok(confFormat.writes(ConferenceService().get(id)))
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   *
   * @return OK with conference in JSON / BadRequest / Forbidden
   */
  def update(id: String) = AuthenticatedAction(parse.json, isREST = true) { request =>
    val conference = request.body.as[Conference]
    conference.uuid = id
    val resp = ConferenceService().update(conference, request.user)

    Ok(confFormat.writes(resp))
  }

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   *
   * @return OK | BadRequest | Forbidden
   */
  def delete(id: String) = AuthenticatedAction(isREST = true) { request =>
    ConferenceService().delete(id, request.user)
    Ok(Json.obj("error" -> false))
  }
}


package controllers.api

import play.api.mvc._
import play.api.libs.json._
import utils.GCAAuth
import utils.serializer.{AccountFormat, ConferenceFormat}
import service.ConferenceService
import utils.DefaultRoutesResolver._
import models.Conference
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
object Conferences extends Controller with GCAAuth {

  implicit val confFormat = new ConferenceFormat()
  val accountFormat = new AccountFormat()
  val conferenceService = ConferenceService()

  /**
   * Create a new conference.
   *
   * @return Created with conference in JSON / BadRequest
   */
  def create = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    val conference = request.body.as[Conference]
    val resp = conferenceService.create(conference, request.user)

    Created(confFormat.writes(resp))
  }

  /**
   * List all available conferences.
   *
   * @return Ok with all conferences publicly available.
   */
  def list = Action { implicit request =>
    Ok(JsArray(
      for (conf <- conferenceService.list()) yield confFormat.writes(conf)
    ))
  }

  /**
   * List all available conferences for which the current user is an owner
   * of at least an abstract
   *
   * @return Ok with all conferences publicly available.
   */
  def listWithOwnAbstracts =  AuthenticatedAction(isREST = true) { implicit request =>
    val conferences = conferenceService.listWithAbstractsOfAccount(request.user)
    Ok(Json.toJson(conferences))
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   *
   * @return OK with conference in JSON / NotFound
   */
  def get(id: String) = Action { implicit request =>
    Ok(confFormat.writes(conferenceService.get(id)))
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   *
   * @return OK with conference in JSON / BadRequest / Forbidden
   */
  def update(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    val conference = request.body.as[Conference]
    conference.uuid = id
    val resp = conferenceService.update(conference, request.user)

    Ok(confFormat.writes(resp))
  }

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   *
   * @return OK | BadRequest | Forbidden
   */
  def delete(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    conferenceService.delete(id, request.user)
    Ok(Json.obj("error" -> false))
  }

  /**
   * Set permissions on the conference.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def setPermissions(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>

    val to_set = for (acc <- request.body.as[List[JsObject]])
      yield accountFormat.reads(acc).get

    val owners = conferenceService.setPermissions(conferenceService.get(id), request.user, to_set)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
   * Get permissions of the conference.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def getPermissions(id: String) = AuthenticatedAction(isREST = true) { implicit request =>

    val owners = conferenceService.getPermissions(conferenceService.get(id), request.user)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }
}


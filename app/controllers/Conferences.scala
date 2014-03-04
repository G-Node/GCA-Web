package controllers

import play.api.mvc._
import utils.serializer.ConferenceFormat
import service.ConferenceService
import play.api.libs.json.JsArray
import utils.URLHelper

import play.api.Play.current


/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
object Conferences extends Controller with OwnerManager with securesocial.core.SecureSocial {

  val httpPrefix = current.configuration.getString("httpPrefix").get

  /**
   * Create a new conference.
   *
   * @return new conference in JSON / Redirect to the conference page
   */
  def create : Action[AnyContent] = TODO

  /**
   * List all available conferences.
   *
   * @return All conferences publicly available.
   */
  def list = Action { request =>
    val confService = ConferenceService() // may be migrated to the constructor
    val formatter = new ConferenceFormat(httpPrefix + request.host) // may be migrated to the constructor
    Ok(JsArray(confService.list().map(formatter.writes(_))))
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   *
   * @return A conference as JSON / page with conference info.
   */
  def get(id: String) : Action[AnyContent] = TODO

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   *
   * @return conference in JSON / conference page
   */
  def update(id: String) : Action[AnyContent] = TODO

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   *
   * @return OK or Failed / Redirect to the conferences list page
   */
  def delete(id: String) : Action[AnyContent] = TODO

}


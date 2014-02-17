package controllers

import play.api.mvc._
import play.api.libs.json._
//import service.ConferenceService
import models.Conference

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */

object Conferences extends Controller {

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
  def list: Action[AnyContent] = Action { implicit request =>
    render {
      case Accepts.Html() => Ok(views.html.index("Your new application is ready."))
      case Accepts.Json() => Ok(Json.toJson("Your new application is ready.").toString())
    }
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

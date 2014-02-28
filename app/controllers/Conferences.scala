package controllers

import play.api.mvc._

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
object Conferences extends Controller with OwnerManager with securesocial.core.SecureSocial {

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
  def list: Action[AnyContent] = TODO

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


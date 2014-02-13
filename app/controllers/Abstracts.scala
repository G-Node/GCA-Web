package controllers

import play.api.mvc._
import play.api.libs.json._
import service.AbstractService
import models.Abstract

/**
 * Abstracts controller.
 * Manages HTTP request logic for abstracts.
 */
object Abstracts extends Controller {

  /**
   * Create a new abstract.
   *
   * @return new abstract in JSON / Redirect to the abstract page
   */
  def create(id: String) : Action[AnyContent] = TODO

  /**
   * List all abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def list_by_event(id: String): Action[AnyContent] = TODO

  /**
   * List all abstracts for a given user.
   *
   * @return All (accessible) abstracts for a given user.
   */
  def list_by_user(id: String): Action[AnyContent] = TODO

  /**
   * An abstract info by id.
   *
   * @param id The id of the abstract.
   *
   * @return An abstract as JSON / abstract page.
   */
  def get(id: String) : Action[AnyContent] = TODO

  /**
   * Update an existing conference info.
   *
   * @param id   The abstract to update.
   *
   * @return abstract in JSON / abstract page
   */
  def update(id: String) : Action[AnyContent] = TODO

  /**
   * Delete an existing abstract.
   *
   * @param id   Abstract id to delete.
   *
   * @return OK or Failed / Redirect to the abstract list page
   */
  def delete(id: String) : Action[AnyContent] = TODO
}

package controllers

import play.api.mvc._

/**
 * Owners manager.
 * Implements logic to add / remove owners to the certain model (if supported).
 */
trait OwnerManager extends Controller {

  /**
   * Set permissions on the object.
   *
   * @return a list of updated permissions (owner IDs) as JSON
   */
  def setPermissions(id: String): Action[AnyContent] = TODO

  /**
   * Lists current object permissions (owner IDs).
   *
   * @return current object permissions (owner IDs).
   */
  def getPermissions(id: String): Action[AnyContent] = TODO

}

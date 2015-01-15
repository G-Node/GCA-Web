package controllers.api

import play.api.libs.json._
import play.api.mvc._
import utils.DefaultRoutesResolver._
import utils.GCAAuth
import utils.serializer.AccountFormat


/**
 * Accounts controller.
 * Manages HTTP request logic for accounts.
 */
object Accounts extends Controller with GCAAuth {

  implicit val accountFormat = new AccountFormat()

  /**
   * Searches for available accounts by email (can be several due to OAuth).
   *
   * @param email an e-mail to search
   *
   * @return Ok with all accounts that match.
   */
  def accountsByEmail(email: String) = AuthenticatedAction(isREST = true) { implicit request =>
    val accounts = getUserStore.findByEmail(email)
    Ok(JsArray(
      for (acc <- accounts) yield accountFormat.writes(acc)
    ))
  }

  def listAccounts() = AuthenticatedAction(isREST = true) { implicit request =>

    if(! request.user.isAdmin) {
      throw new IllegalAccessException("Need to be a site admin to obtain account list!")
    }

    val accounts = getUserStore.list()
    Ok(Json.toJson(accounts))
  }
}
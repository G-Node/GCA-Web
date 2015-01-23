package controllers.api

import play.api.libs.json._
import play.api.mvc._
import utils.DefaultRoutesResolver._
import utils.serializer.AccountFormat
import models._
import service.AccountStore

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}

/**
 * Accounts controller.
 * Manages HTTP request logic for accounts.
 */
class Accounts(implicit val env: Environment[Login, CachedCookieAuthenticator])
extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val accountFormat = new AccountFormat()

  val accountStore = new AccountStore()

  /**
   * Searches for available accounts by email (can be several due to OAuth).
   *
   * @param email an e-mail to search
   *
   * @return Ok with all accounts that match.
   */
  def accountsByEmail(email: String) = SecuredAction { implicit request =>
    val accounts = accountStore.findByEmail(email)
    Ok(JsArray(
      for (acc <- accounts) yield accountFormat.writes(acc)
    ))
  }

  def listAccounts() = SecuredAction { implicit request =>

    if(! request.identity.account.isAdmin) {
      throw new IllegalAccessException("Need to be a site admin to obtain account list!")
    }

    val accounts = accountStore.list()
    Ok(Json.toJson(accounts))
  }

}
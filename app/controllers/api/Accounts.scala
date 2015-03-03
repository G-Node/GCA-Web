package controllers.api

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models._
import play.api.libs.json._
import service.AccountStore
import utils.DefaultRoutesResolver._
import utils.serializer.AccountFormat

/**
 * Accounts controller.
 * Manages HTTP request logic for accounts.
 */
class Accounts(implicit val env: Environment[Login, CachedCookieAuthenticator])
extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val accountFormat = new AccountFormat()

  val accountStore = new AccountStore()

  /**
   * Searches for available accounts by email.
   *
   * @param email an e-mail to search
   *
   * @return Ok with all accounts that match.
   */
  def accountsByEmail(email: String) = SecuredAction { implicit request =>
    // TODO since email is unique this could be a single result
    val accounts = try {
      Seq(accountStore.getByMail(email))
    } catch {
      case e: Throwable => Seq[Account]()
    }

    Ok(Json.toJson(accounts))
  }

  def listAccounts() = SecuredAction { implicit request =>

    if(! request.identity.account.isAdmin) {
      throw new IllegalAccessException("Need to be a site admin to obtain account list!")
    }

    val accounts = accountStore.list()
    Ok(Json.toJson(accounts))
  }

}
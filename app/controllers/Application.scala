package controllers

import play.api._
import play.api.mvc._
import utils.GCAAuth
import models.{Conference, Account, Abstract}
import service.ConferenceService

object Application extends Controller with GCAAuth {

  def index = AccountAwareAction { implicit request =>
    val conference = ConferenceService().list()(0)

    Redirect(routes.Application.conference(conference.uuid))
  }

  def showUserInfo = AccountAwareAction { implicit request =>
    val userName = request.user match {
      case Some(account: Account) => account.fullName + " [" + account.firstName + " " + account.lastName + "]"
      case _ => "guest"
    }

    Logger.debug(request.uri)
    Logger.debug(request.domain)
    Logger.debug(request.host)
    Ok("Hello %s".format(userName))
  }

  def showSecret = AuthenticatedAction { implicit request =>
    Logger.debug(request.user.toString)

    Ok("The answer is 42.")
  }

  def submission(id: String) = UserAwareAction { implicit request => // TODO should be a secure action
    val user: Account = request.user match {
      case Some(user: Account) => user
      case _                   => null
    }

    val srv = ConferenceService()
    val conf = srv.get(id)
    Ok(views.html.submission(user, conf, None))
  }

  def abstractsPublic(confId: String) = AccountAwareAction { implicit request =>
    val confServ = ConferenceService()
    val conference = confServ.get(confId)

    Ok(views.html.abstractlist(request.user, conference))
  }

  def abstractsPrivate = AuthenticatedAction(isREST = false) { implicit request =>
    val conference = ConferenceService().list()(0)

    // TODO all private abstracts for owner
    Ok(views.html.abstractlist(Some(request.user), conference))
  }

  def abstractsPending = AuthenticatedAction(isREST = false) { implicit request =>
    val conference = ConferenceService().list()(0)

    // TODO all abstracts for reviewers
    Ok(views.html.abstractlist(Some(request.user), conference))
  }

  def conference(confId: String) = AccountAwareAction { implicit request =>
    val conference = ConferenceService().get(confId)

    Ok(views.html.conference(request.user, conference))
  }

  def contact = AccountAwareAction { implicit request =>
    val conference = ConferenceService().list()(0)

    Ok(views.html.contact(request.user, conference))
  }

  def impressum = AccountAwareAction { implicit request =>
    val conference = ConferenceService().list()(0)

    Ok(views.html.impressum(request.user, conference))
  }
}

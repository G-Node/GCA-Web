package controllers

import play.api._
import play.api.mvc._
import utils.GCAAuth
import models._
import service.{AbstractService, ConferenceService}
import java.net._

object Application extends Controller with GCAAuth {

  val abstractService = AbstractService()
  val conferenceService = ConferenceService()
  
  def index = AccountAwareAction { implicit request =>
    val conference = conferenceService.list()(0)

    val link = URLEncoder.encode(conference.short, "UTF-8")
    Redirect(routes.Application.conference(link))
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

    val conf = conferenceService.get(id)
    Ok(views.html.submission(user, conf, None))
  }

  def edit(id: String) = AuthenticatedAction(isREST = false) { implicit request =>
    val abstr = abstractService.getOwn(id, request.user)

    Ok(views.html.submission(request.user, abstr.conference, Option(abstr)))
  }

  def abstractsPublic(confId: String) = AccountAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.abstractlist(request.user, conference))
  }

  def abstractsPrivate = AuthenticatedAction(isREST = false) { implicit request =>

    Ok(views.html.dashboard.user(request.user))
  }

  def abstractsPending = AuthenticatedAction(isREST = false) { implicit request =>
    val conference = conferenceService.list()(0)

    // TODO all abstracts for reviewers
    Ok(views.html.abstractlist(Some(request.user), conference))
  }

  def conference(confId: String) = AccountAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.conference(request.user, conference))
  }

  def contact = AccountAwareAction { implicit request =>
    Ok(views.html.contact(request.user))
  }

  def impressum = AccountAwareAction { implicit request =>
    Ok(views.html.impressum(request.user))
  }

  def about = AccountAwareAction { implicit request =>
    Ok(views.html.about(request.user))
  }

  def createConference() = AuthenticatedAction { implicit request =>

    if (!request.user.isAdmin) {
      Unauthorized(views.html.error.NotAuthorized())
    } else {
      Ok(views.html.dashboard.admin.conference(request.user, None))
    }
  }

  def adminConference(confId: String) = AuthenticatedAction { implicit request =>
    val conference = conferenceService.get(confId)

    if (!(conference.isOwner(request.user) || request.user.isAdmin)) {
      Unauthorized("Not allowed!")
    } else {
      Ok(views.html.dashboard.admin.conference(request.user, Some(conference)))
    }
  }


  def adminAbstracts(confId: String) = AuthenticatedAction { implicit request =>
    val conference = conferenceService.get(confId)

    if (!(conference.isOwner(request.user) || request.user.isAdmin)) {
      Unauthorized("Not allowed!")
    } else {
      Ok(views.html.dashboard.admin.abstracts(request.user, conference))
    }
  }

  def adminAccounts() = AuthenticatedAction { implicit request =>
    if (!request.user.isAdmin) {
      Unauthorized("Only site administrators are allowed!")
    } else {
      Ok(views.html.dashboard.admin.accounts(request.user))
    }
  }

  def viewAbstract(id: String) = AccountAwareAction { implicit request =>
    val abstr = request.user match {
      case Some(account) => abstractService.getOwn(id, account)
      case _             => abstractService.get(id)
    }

    Ok(views.html.abstractviewer(request.user, abstr.conference, abstr))
  }

}

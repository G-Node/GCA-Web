package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import play.api._
import play.api.mvc._
import models._
import service.{AbstractService, ConferenceService}
import java.net._
import org.joda.time.DateTime

class Application(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  val abstractService = AbstractService()
  val conferenceService = ConferenceService()
  
  def index = UserAwareAction { implicit request =>
    Redirect(routes.Application.conferences())
  }

  def showUserInfo = UserAwareAction { implicit request =>

    val userName = request.identity match {
      case Some(id: Login) => id.account.fullName + " [" + id.account.firstName + " " + id.account.lastName + "]"
      case _ => "guest"
    }

    Logger.debug(request.uri)
    Logger.debug(request.domain)
    Logger.debug(request.host)
    Ok("Hello %s".format(userName))
  }


  def submission(id: String) = UserAwareAction { implicit request => // TODO should be a secure action
    val user: Account = request.identity match {
      case Some(id: Login) => id.account
      case _               => null
    }

    val conf = conferenceService.get(id)
    Ok(views.html.submission(user, conf, None))
  }

  def edit(id: String) = SecuredAction { implicit request =>
    val abstr = abstractService.getOwn(id, request.identity.account)

    Ok(views.html.submission(request.identity.account, abstr.conference, Option(abstr)))
  }

  def abstractsPublic(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.abstractlist(request.identity.map{ _.account }, conference))
  }

  def locations(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.locations(request.identity.map{ _.account }, conference))
  }

  def floorplans(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.floorplans(request.identity.map{ _.account }, conference))
  }

  def abstractsPrivate = SecuredAction { implicit request =>

    Ok(views.html.dashboard.user(request.identity.account))
  }

  def abstractsPending = SecuredAction { implicit request =>
    val conference = conferenceService.list()(0)

    // TODO all abstracts for reviewers
    Ok(views.html.abstractlist(Some(request.identity.account), conference))
  }

  def conferences = UserAwareAction { implicit request =>
    val conferences = conferenceService.list()

    val list_active = conferences.filter(conf => conf.isActive)
    val list_other = conferences.filter(conf => !conf.isActive)

    Ok(views.html.conferencelist(request.identity.map{ _.account }, list_active, list_other))
  }

  def conference(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.conference(request.identity.map{ _.account }, conference))
  }

  def schedule(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.conferenceschedule(request.identity.map{ _.account }, conference))
  }

  def contact = UserAwareAction { implicit request =>
    Ok(views.html.contact(request.identity.map{ _.account }))
  }

  def impressum = UserAwareAction { implicit request =>
    Ok(views.html.impressum(request.identity.map{ _.account }))
  }

  def about = UserAwareAction { implicit request =>
    Ok(views.html.about(request.identity.map{ _.account }))
  }

  def createConference() = SecuredAction { implicit request =>

    if (!request.identity.account.isAdmin) {
      Unauthorized(views.html.error.NotAuthorized())
    } else {
      Ok(views.html.dashboard.admin.conference(request.identity.account, None))
    }
  }

  def adminConference(confId: String) = SecuredAction { implicit request =>
    val conference = conferenceService.get(confId)

    if (!(conference.isOwner(request.identity.account) || request.identity.account.isAdmin)) {
      Unauthorized("Not allowed!")
    } else {
      Ok(views.html.dashboard.admin.conference(request.identity.account, Some(conference)))
    }
  }

  def adminAbstracts(confId: String) = SecuredAction { implicit request =>
    val conference = conferenceService.get(confId)

    if (!(conference.isOwner(request.identity.account) || request.identity.account.isAdmin)) {
      Unauthorized("Not allowed!")
    } else {
      Ok(views.html.dashboard.admin.abstracts(request.identity.account, conference))
    }
  }

  def adminAccounts() = SecuredAction { implicit request =>
    if (!request.identity.account.isAdmin) {
      Unauthorized("Only site administrators are allowed!")
    } else {
      Ok(views.html.dashboard.admin.accounts(request.identity.account))
    }
  }

  def viewAbstract(id: String) = UserAwareAction { implicit request =>
    val abstr = request.identity match {
      case Some(uid) => abstractService.getOwn(id, uid.account)
      case _         => abstractService.get(id)
    }

    Ok(views.html.abstractviewer(request.identity.map{ _.account }, abstr.conference, abstr))
  }

}

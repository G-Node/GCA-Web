package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}
import play.api._
import play.api.mvc._
import models._
import service.{AbstractService, ConferenceService}
import java.net._
import org.joda.time.DateTime
import scala.collection.JavaConverters._

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
    try {
      val abstr = abstractService.getOwn(id, request.identity.account)

      Ok(views.html.submission(request.identity.account, abstr.conference, Option(abstr)))
    } catch {
      case ia: IllegalAccessException => Forbidden(views.html.error.NotAuthorized())
    }
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

  def abstractsFavourite = SecuredAction { implicit request =>
    Ok(views.html.dashboard.favouriteabstracts(request.identity.account))
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

  def datenschutz = UserAwareAction { implicit request =>
    Ok(views.html.datenschutz(request.identity.map{ _.account }))
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

  def createAppCacheManifest() = UserAwareAction { implicit request =>
    var dynamicViews: String = "";
    var conf = conferenceService.list().head
    dynamicViews += s"""
                       |/conference/${conf.short}
                       |/conference/${conf.short}/schedule
                       |/conference/${conf.short}/submission
                       |/conference/${conf.short}/floorplans
                       |/conference/${conf.short}/locations
                       |/conference/${conf.short}/abstracts
                       |${conf.logo}
                       |${conf.thumbnail}""".stripMargin
    for (abs: Abstract <- conf.abstracts.asScala) {
      dynamicViews +=
        s"""
           |/abstracts/${abs.uuid}""".stripMargin
      for (fig: Figure <- abs.figures.asScala) {
        dynamicViews +=
          s"""
             |/api/figures/${fig.uuid}/imagemobile""".stripMargin
      }
    }

    Ok(
      s"""CACHE MANIFEST
         |# v1.0.2
         |# Views
         |/conferences
         |/contact
         |/about
         |/impressum
         |/datenschutz
         |# Assets
         |/assets/manifest.json
         |/assets/lib/momentjs/moment.js
         |/assets/lib/bootstrap/js/bootstrap.min.js
         |/assets/lib/bootstrap/js/bootstrap.js
         |/assets/stylesheets/g-node-bootstrap.play.css
         |/assets/lib/jquery/jquery.js
         |/assets/lib/jquery/jquery.min.js
         |/assets/lib/jquery-ui/jquery-ui.min.css
         |/assets/lib/jquery-ui/jquery-ui.js
         |/assets/stylesheets/layout.css
         |/assets/javascripts/require.js
         |/assets/lib/requirejs/require.js
         |/assets/lib/sammy/sammy.js
         |/assets/images/favicon.png
         |/assets/images/bccn.png
         |/assets/images/gnode_logo.png
         |/assets/fonts/glyphicons-halflings-regular.eot
         |/assets/fonts/glyphicons-halflings-regular.svg
         |/assets/fonts/glyphicons-halflings-regular.ttf
         |/assets/fonts/glyphicons-halflings-regular.woff
         |/assets/javascripts/knockout-sortable.min.js
         |# leaflet
         |/assets/javascripts/lib/leaflet/leaflet.css
         |/assets/javascripts/lib/leaflet/leaflet.js
         |/assets/javascripts/lib/leaflet/leaflet-src.js
         |/assets/javascripts/lib/leaflet/images/layers.png
         |/assets/javascripts/lib/leaflet/images/layers-2x.png
         |/assets/javascripts/lib/leaflet/images/marker-icon.png
         |/assets/javascripts/lib/leaflet/images/marker-icon-2x.png
         |/assets/javascripts/lib/leaflet/images/marker-shadow.png
         |# scheduler
         |/assets/javascripts/lib/scheduler/dhtmlxscheduler.css
         |/assets/javascripts/lib/scheduler/dhtmlxscheduler.js
         |/assets/javascripts/lib/scheduler/ext/dhtmlxscheduler_readonly.js
         |# libs
         |/assets/javascripts/lib/accessors.js
         |/assets/javascripts/lib/astate.js
         |/assets/javascripts/lib/models.js
         |/assets/javascripts/lib/msg.js
         |/assets/javascripts/lib/multi.js
         |/assets/javascripts/lib/offline.js
         |/assets/javascripts/lib/owned.js
         |/assets/javascripts/lib/tools.js
         |/assets/javascripts/lib/update-storage.js
         |/assets/javascripts/lib/validate.js
         |# View Models
         |/assets/javascripts/abstract-list.js
         |/assets/javascripts/abstract-viewer.js
         |/assets/javascripts/abstract-favourite.js
         |/assets/javascripts/browser.js
         |/assets/javascripts/conference-schedule.js
         |/assets/javascripts/config.js
         |/assets/javascripts/editor.js
         |/assets/javascripts/locations.js
         |/assets/javascripts/main.js
         |/assets/javascripts/userdash.js
         |
         |https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.1/jquery-ui-timepicker-addon.min.js
         |https://cdnjs.cloudflare.com/ajax/libs/knockout/3.0.0/knockout-debug.js
         |https://cdnjs.cloudflare.com/ajax/libs/jquery-ui-timepicker-addon/1.6.1/jquery-ui-timepicker-addon.min.css
         |https://fonts.googleapis.com/css?family=EB+Garamond|Open+Sans
         |https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.3/MathJax.js?delayStartupUntil=configured
         |https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.3/extensions/MathMenu.js
         |
         |# Styles
         |/assets/stylesheets/_g-node-bootstrap.less
         |/assets/stylesheets/g-node-bootstrap.play.less
         |/assets/stylesheets/layout.less
         |/assets/stylesheets/Readme.md
         |/assets/stylesheets/custom/_classes.less
         |/assets/stylesheets/custom/_classes_conference_scheduler.less
         |/assets/stylesheets/custom/_colors.less
         |/assets/stylesheets/custom/_font.less
         |/assets/stylesheets/custom/bootstrap/_custom-colors.less
         |/assets/stylesheets/custom/bootstrap/_custom-elements.less
         |/assets/stylesheets/custom/bootstrap/_custom-fonts.less
         |/assets/stylesheets/custom/bootstrap/_custom-vars.less
         |
         |# Dynamic Views
         |${dynamicViews}
         |
         |NETWORK:
         |*
         |http:/*
         |https:/*
        """.stripMargin).as("text/cache-manifest")
  }

}

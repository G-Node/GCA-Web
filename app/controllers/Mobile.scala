package controllers

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import models.Login
import service.{AbstractService, ConferenceService}

/**
  * Created by garbers on 04.05.17.
  */
class Mobile (implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  val abstractService = AbstractService()
  val conferenceService = ConferenceService()

  def Conferences = UserAwareAction { implicit request =>
    val conferences = conferenceService.list()

    Ok(views.html.mob_conferencelist(conferences))
  }

  def Conference(confId: String) = UserAwareAction { implicit request =>
    val conference = conferenceService.get(confId)

    Ok(views.html.mob_abstractlist(conference))
  }
}

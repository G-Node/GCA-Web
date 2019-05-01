package controllers.api

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Environment, Silhouette}
import javax.persistence.NoResultException
import models._
import play.api.libs.json.{JsArray, _}
import service.{BannerService, ConferenceService, BannerMobileService}
import utils.DefaultRoutesResolver._
import utils.serializer.BannerFormat

import scala.collection.JavaConversions._

/**
  * Banner controller.
  * Manages HTTP request logic for upload/download banner for conferences.
  */
class Banners(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val banFormat = new BannerFormat()
  val conferenceService = ConferenceService()
  val bannerService = BannerService()
  val bannerMobileService = BannerMobileService()

  /**
    * Upload file with a banner to the specified conference (id).
    * An incoming request Content-Type should be multipart/form-data.
    * Request should contain minimum two parts:
    * - "file" with associated File data
    * - "banner" with associated JSON string with Banner data
    *
    * @param id  The id of the conference.
    *
    * @return  OK / Failed
    */
  def upload(id: String) = SecuredAction(parse.multipartFormData) { implicit request =>
    val conference = conferenceService.getOwn(id, request.identity.account)
    val tempfile = request.body.file("file").map {
      banner => banner.ref
    }.getOrElse {
      throw new IllegalArgumentException("File is missing")
    }

    val jsban = Json.parse(request.body.dataParts("banner").head).as[Banner]

    val banner = bannerService.create(jsban, tempfile, conference, request.identity.account)

    Created(banFormat.writes(banner))
  }

  /**
    * Download banner from the specified conference (id).
    *
    * @param id  The id of the conference.
    *
    * @return  OK / Failed
    */
  def list(id: String) = UserAwareAction { implicit request =>
    Ok(JsArray(
      for (ban <- asScalaSet(
        conferenceService.get(id).banner
      ).toSeq
      ) yield banFormat.writes(ban)
    ))
  }

  /**
    * Download banner file from the specified banner object (id).
    *
    * @param id  The id of the banner.
    *
    * @return  OK / Failed
    */
  def download(id: String) = UserAwareAction { implicit request =>
    Ok.sendFile(bannerService.openFile(
      bannerService.get(id)
    ))
  }

  /**
    * Download mobile banner file from the specified banner object (id).
    *
    * @param id  The id of the banner.
    *
    * @return  OK / Failed
    */
  def downloadmobile(id: String) = UserAwareAction { implicit request =>
    Ok.sendFile(bannerMobileService.openFile(
      bannerMobileService.get(id)
    ))
  }

  /**
    * Delete an existing banner (id).
    *
    * @param id   The id of the banner.
    *
    * @return  OK / Failed
    */
  def delete(id: String) = SecuredAction { implicit request =>
    bannerService.delete(id, request.identity.account)
    Ok("Banner deleted successfully.")
  }

}

package controllers.api

import org.apache.commons.codec.digest.DigestUtils
import play.api.mvc._
import play.api.libs.json._
import utils.serializer.{AccountFormat, ConferenceFormat}
import service.ConferenceService
import utils.DefaultRoutesResolver._
import models.Conference
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import models._
import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */
class Conferences(implicit val env: Environment[Login, CachedCookieAuthenticator])
  extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val confFormat = new ConferenceFormat()
  val accountFormat = new AccountFormat()
  val conferenceService = ConferenceService()

  /**
   * Create a new conference.
   *
   * @return Created with conference in JSON / BadRequest
   */
  def create = SecuredAction(parse.json) { implicit request =>
    val conference = request.body.as[Conference]
    val resp = conferenceService.create(conference, request.identity.account)

    Created(confFormat.writes(resp)).withHeaders(ETAG -> conference.eTag)
  }

  def resultWithETag[A](conferences: Seq[Conference])(implicit request: Request[A]) = {
    val theirs = request.headers.get("If-None-Match")
    val eTag = conferences.map(_.eTag).reduce((a, b) => DigestUtils.md5Hex(a + b))

    if (theirs.contains(eTag)) {
      NotModified
    } else {
      Ok(Json.toJson(conferences)).withHeaders(ETAG -> eTag)
    }
  }

  /**
   * List all available conferences.
   *
   * @return Ok with all conferences publicly available.
   */
  def list(group: String) = Action { implicit request =>
    val conferences = if (group != null) {
      conferenceService.listWithGroup(group)
    } else {
      conferenceService.list()
    }

    resultWithETag(conferences)
  }

  /**
   * List all available conferences for which the current user is an owner
   * of at least an abstract
   *
   * @return Ok with all conferences publicly available. / empty string to circumvent error message
   */
  def listWithOwnAbstracts =  SecuredAction { implicit request =>
    val conferences = conferenceService.listWithAbstractsOfAccount(request.identity.account)
    if (conferences.length==0) {
      Ok(Json.toJson(""))
    } else {
      resultWithETag(conferences)
    }
  }

  /**
    * List all available conferences for which the current user is an owner
    * of at least an abstract
    *
    * @return Ok with all conferences publicly available.
    */
  def listWithFavAbstracts =  SecuredAction { implicit request =>
    val conferences = conferenceService.listWithFavouriteAbstractsOfAccount(request.identity.account)
    if (conferences.length==0) {
      BadRequest("No favourite abstracts")
    } else {
      resultWithETag(conferences)
    }
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   * @return OK with conference in JSON / NotFound
   */
  def get(id: String) = Action { implicit request =>

    val conference = conferenceService.get(id)

    val theirs = request.headers.get("If-None-Match")
    val eTag = conference.eTag

    if (theirs.contains(eTag)) {
       NotModified
    } else {
      Ok(confFormat.writes(conference)).withHeaders(ETAG -> eTag)
    }
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   * @return OK with conference in JSON / BadRequest / Forbidden
   */
  def update(id: String) = SecuredAction(parse.json) { implicit request =>
    val conference = request.body.as[Conference]
    conference.uuid = id
    val resp = conferenceService.update(conference, request.identity.account)

    Ok(confFormat.writes(resp)).withHeaders(ETAG -> conference.eTag)
  }

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   * @return OK | BadRequest | Forbidden
   */
  def delete(id: String) = SecuredAction { implicit request =>
    conferenceService.delete(id, request.identity.account)
    Ok(Json.obj("error" -> false))
  }

  /**
   * Set permissions on the conference.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def setPermissions(id: String) = SecuredAction(parse.json) { implicit request =>

    val to_set = for (acc <- request.body.as[List[JsObject]])
      yield accountFormat.reads(acc).get

    val owners = conferenceService.setPermissions(conferenceService.get(id), request.identity.account, to_set)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
   * Get permissions of the conference.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def getPermissions(id: String) = SecuredAction { implicit request =>

    val owners = conferenceService.getPermissions(conferenceService.get(id), request.identity.account)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
    * Set the geo entry of a specific conference.
    *
    * @param id Conference id where the geo entry should be set.
    * @return OK | BadRequest | Forbidden | Unauthorized
    */
  def setGeo(id: String) = SecuredAction(parse.json) { implicit request =>
    val geoContent = Json.stringify(request.body)
    conferenceService.updateSpecificFields(conferenceService.get(id),
                                            request.identity.account, geo = geoContent)
    Ok(request.body)
  }

  /**
    * Return the geo entry of a specific conference.
    *
    * @param id Conference id of the required geo entry.
    * @return OK | NotFound
    */
  def getGeo(id: String) = UserAwareAction { implicit request =>
    val geo = conferenceService.get(id).geo
    if (geo == null) {
      NotFound(Json.obj("message" -> "Geo entry not found."))
    } else {
      val theirs = request.headers.get("If-None-Match")
      val eTag = DigestUtils.md5Hex(geo)
      if (theirs.contains(eTag)) {
        NotModified
      } else {
        Ok(Json.parse(geo)).withHeaders(ETAG -> eTag)
      }
    }
  }

  /**
    * Set the schedule entry of a specific conference.
    *
    * @param id Conference id where the schedule entry should be set.
    * @return OK | BadRequest | Forbidden | Unauthorized
    */
  def setSchedule(id: String) = SecuredAction(parse.json) { implicit request =>
    val scheduleContent = Json.stringify(request.body)
    conferenceService.updateSpecificFields(conferenceService.get(id),
                                            request.identity.account, schedule = scheduleContent)
    Ok(request.body)
  }

  /**
    * Return the schedule entry of a specific conference.
    *
    * @param id Conference id of the required schedule entry.
    * @return OK | NotFound
    */
  def getSchedule(id: String) = UserAwareAction { implicit request =>
    val schedule = conferenceService.get(id).schedule
    if (schedule == null) {
      NotFound(Json.obj("message" -> "Schedule entry not found."))
    } else {
      val theirs = request.headers.get("If-None-Match")
      val eTag = DigestUtils.md5Hex(schedule)
      if (theirs.contains(eTag)) {
        NotModified
      } else {
        Ok(Json.parse(schedule)).withHeaders(ETAG -> eTag)
      }
    }
  }

  /**
    * Set the info entry of a specific conference.
    *
    * @param id Conference id where the info entry should be set.
    * @return OK | BadRequest | Forbidden | Unauthorized
    */
  def setInfo(id: String) = SecuredAction(parse.text) { implicit request =>
    val infoContent = request.body
    conferenceService.updateSpecificFields(conferenceService.get(id), request.identity.account, info = infoContent)
    Ok(request.body)
  }

  /**
    * Return the info entry of a specific conference.
    *
    * @param id Conference id of the required info entry.
    * @return OK | NotFound
    */
  def getInfo(id: String) = UserAwareAction { implicit request =>
    val info = conferenceService.get(id).info
    if (info == null) {
      NotFound(Json.obj("message" -> "Info entry not found."))
    } else {
      val theirs = request.headers.get("If-None-Match")
      val eTag = DigestUtils.md5Hex(info)
      if (theirs.contains(eTag)) {
        NotModified
      } else {
        Ok(info).withHeaders(ETAG -> eTag)
      }
    }
  }

}

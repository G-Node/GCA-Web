package controllers.api

import org.apache.commons.codec.digest.DigestUtils
import org.joda.time.format.DateTimeFormat
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsArray, JsObject, Json, _}
import play.api.mvc._
import service._
import utils.DefaultRoutesResolver._
import utils.serializer.{AbstractFormat, AccountFormat, StateLogWrites}
import models._

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}

/**
 * Abstracts controller.
 * Manages HTTP request logic for abstracts.
 */
class Abstracts(implicit val env: Environment[Login, CachedCookieAuthenticator])
extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val absFormat = new AbstractFormat()
  val accountFormat = new AccountFormat()
  val abstractService = AbstractService()
  val conferenceService = ConferenceService()

  //RFC 1123
  val rfcDateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC()

  /**
   * Create a new abstract.
   *
   * @return new abstract in JSON / Redirect to the abstract page
   */
  def create(id: String) = SecuredAction(parse.json) { implicit request =>

    val abs = request.body.as[Abstract]
    val conference = conferenceService.get(id)

    if(!conference.isOpen &&
      !(request.identity.account.isAdmin || conference.isOwner(request.identity.account))) {
      throw new IllegalAccessException("Conference is closed!")
    }

    val newAbs = abstractService.create(abs, conference, request.identity.account)

    Created(Json.toJson(newAbs)).withHeaders(ETAG -> newAbs.eTag)
  }

  def resultWithETag[A](abstracts: Seq[Abstract])(implicit request: Request[A]) = {
    val theirs = request.headers.get("If-None-Match")
    var eTag = DigestUtils.md5Hex("empty")
    if (!abstracts.isEmpty) {
      eTag = abstracts.map(_.eTag).reduce((a, b) => DigestUtils.md5Hex(a + b))
    }

    if (theirs.contains(eTag)) {
      NotModified
    } else {
      Ok(Json.toJson(abstracts)).withHeaders(ETAG -> eTag)
    }
  }

  /**
   * List all published abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listByConference(id: String) = UserAwareAction { implicit request =>

    val conference = conferenceService.get(id)
    val abstracts = abstractService.list(conference)

    resultWithETag(abstracts)
  }

  /**
   * List all abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listAllByConference(id: String) = SecuredAction {  implicit request =>

    val conference = conferenceService.get(id)

    if (!(request.identity.account.isAdmin || conference.isOwner(request.identity.account))) {
      throw new IllegalAccessException("Not allowed")
    }

    val abstracts = abstractService.listAll(conference)
    resultWithETag(abstracts)
  }

  /**
   * List all abstracts for a given user.
   *
   * @return All (accessible) abstracts for a given user.
   */
  def listByAccount(id: String) = SecuredAction { implicit request =>
    val ownAbstracts = abstractService.listOwn(request.identity.account)
    resultWithETag(ownAbstracts)
  }

  /**
    * List all favourite abstracts for a given user.
    *
    * replace Own by Favourite
    *
    * @return All (accessible) abstracts for a given user.
    */
  def listFavByAccount(id: String) = SecuredAction { implicit request =>
    val favAbstracts = abstractService.listFavourite(request.identity.account)
    resultWithETag(favAbstracts)
  }

  /**
   * List all abstracts for a given conference and a given user
   *
   * @return All (accessible) abstracts for a given user.
   */
  def listOwn(conferenceId: String) = SecuredAction { implicit request =>

    val conference = conferenceService.get(conferenceId)
    val abstracts = abstractService.listOwn(conference, request.identity.account)

    resultWithETag(abstracts)
  }

  /**
    * List all favourite abstracts for a given conference and a given user
    *
    * @return All (accessible) favourite abstracts for a given user.
    */
  def listFavByConf(conferenceId: String) = SecuredAction { implicit request =>
    val conference = conferenceService.get(conferenceId)
    val abstracts = abstractService.listFavourite(conference, request.identity.account)
    resultWithETag(abstracts)
  }

  /**
    * List all favourite abstracts for a given conference and a given user
    *
    * @return All (accessible) favourite abstracts for a given user.
    */
  def listFavUuidByConf(conferenceId: String) = SecuredAction { implicit request =>
    val conference = conferenceService.get(conferenceId)
    val abstracts = abstractService.listIsFavourite(conference, request.identity.account)
    Ok(Json.toJson(
      for (abs <- abstracts) yield abs.uuid
    ))
  }

  /**
   * An abstract info by id.
   *
   * @param id The id of the abstract.
   *
    * @return An abstract as JSON / abstract page.
   */
  def get(id: String) = UserAwareAction { implicit request =>
    Logger.debug(s"Getting abstract with uuid: [$id]")

    val abs = request.identity.map { _.account } match {
      case Some(user) => abstractService.getOwn(id, user)
      case _          => abstractService.get(id)
    }

    if (request.headers.get("If-None-Match").contains(abs.eTag)) {
      NotModified
    } else {
      Ok(Json.toJson(abs)).withHeaders(
        LAST_MODIFIED -> rfcDateFormatter.print(abs.mtime),
        ETAG -> abs.eTag)
      }
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The abstract to update.
   *
   * @return abstract in JSON / abstract page
   */
  def update(id: String) = SecuredAction(parse.json) { implicit request =>
    Logger.debug(s"Updating abstract with uuid: [$id]")

    val abs = request.body.as[Abstract]

    if (abs.uuid != null && abs.uuid != id) {
      //TODO: should that be allowed? I guess not - ck
      throw new RuntimeException("Trying to change the id of an abstract!")
      Logger.debug(s"Updating [$id]: UUID mismatch")
    }

    val oldAbstract = abstractService.getOwn(abs.uuid, request.identity.account)
    val conference = oldAbstract.conference

    if(!conference.isOpen && oldAbstract.state != AbstractState.InRevision && !request.identity.account.isAdmin) {
      throw new IllegalAccessException("Conference is closed and abstract not in 'InRevision' state!")
    }
    
    val newAbstract = abstractService.update(abs, request.identity.account)

    Ok(Json.toJson(newAbstract)).withHeaders(ETAG -> newAbstract.eTag)
  }

  /**
   * Delete an existing abstract.
   *
   * @param id   Abstract id to delete.
   *
   * @return OK or Failed / Redirect to the abstract list page
   */
  def delete(id: String) = SecuredAction { implicit request =>
    Logger.debug(s"Deleting abstract with uuid: [$id]")

    abstractService.delete(id, request.identity.account)

    Ok("Abstract Deleted") //FIXME: JSON
  }

  /**
   * Set permissions on the abstract.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def setPermissions(id: String) = SecuredAction(parse.json) { implicit request =>

    val to_set = for (acc <- request.body.as[List[JsObject]])
      yield accountFormat.reads(acc).get

    val abstr = abstractService.getOwn(id, request.identity.account)
    val owners = abstractService.setPermissions(abstr, request.identity.account, to_set)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
   * Get permissions of the abstract.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def getPermissions(id: String) = SecuredAction { implicit request =>

    val abstr = abstractService.getOwn(id, request.identity.account)
    val owners = abstractService.getPermissions(abstr, request.identity.account)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
    * Get favourite users of the abstract.
    *
    * @return a list of updated permissions (accounts) as JSON
    */
  def favouriteUsers(id: String) = SecuredAction { implicit request =>
    val abstr = abstractService.getFav(id, request.identity.account)
    val favUsers = abstractService.getFavouriteUsers(abstr, request.identity.account)
    Ok(JsArray(
      for (acc <- favUsers) yield accountFormat.writes(acc)
    ))
  }

  /**
    * Add the logged in user to the favourite users list of an abstract.
    *
    * @return The id of the updated Abstract as JSON
    */
  def addFavUser(id: String) = SecuredAction { implicit request =>
    val abstr = abstractService.get(id)
    abstractService.addFavUser(abstr, request.identity.account)
    Ok(Json.toJson(id))
  }
  /**
    * Remove the logged in user from the favourite users list of an abstract.
    *
    * @return The id of the updated Abstract as JSON
    */
  def removeFavUser(id: String) = SecuredAction { implicit request =>
    val abstr = abstractService.get(id)
    abstractService.removeFavUser(abstr, request.identity.account)
    Ok(Json.toJson(id))
  }

  def listState(id: String) = SecuredAction { implicit request =>
    implicit val logWrites = new StateLogWrites()

    Ok(Json.toJson(abstractService.listStates(id, request.identity.account)))
  }


  def setState(id: String) = SecuredAction(parse.json) { implicit request =>
    implicit val logWrites = new StateLogWrites()
    val changeReads = ((__ \ "state").read[String] and (__ \ "note").readNullable[String]).tupled

    val account = request.identity.account
    val abstr = abstractService.getOwn(id, account) // TODO: will not work for admin (GitHub issue, #155)
    val conference = abstr.conference

    val (toState, msg): (AbstractState.State, Option[String]) = changeReads.reads(request.body).fold (
      invalid = {errors => throw new IllegalArgumentException("Invalid state change object") },
      valid = { case(s, m) => (AbstractState.withName(s), m) }
    )

    val fromState = abstr.state

    val isAdmin = account.isAdmin || conference.owners.contains(account)
    val isOwner = abstr.isOwner(account)

    val canTransitionAdmin = isAdmin && fromState.canTransitionTo(toState, isAdmin=true, conference.isOpen)
    val canTransitionOwner = isOwner && fromState.canTransitionTo(toState, isAdmin=false, conference.isOpen)

    if (!(canTransitionAdmin || canTransitionOwner)) {
      throw new IllegalAccessException(s"No permission to set state to $toState")
    }

    val stateLog = abstractService.setState(abstr, toState, account, msg)

    Ok(Json.toJson(stateLog))
  }

  def patch(id: String) = SecuredAction(parse.json) { implicit request =>
    val patchReads = Reads.list(((__ \ "op").read[String] and
      (__ \ "path").read[String] and (__ \ "value").readNullable[JsValue]).tupled)

    val account = request.identity.account
    val abstr = abstractService.getOwn(id, account)
    val isAdmin = account.isAdmin || abstr.conference.owners.contains(account)

    if (!isAdmin) {
      //patching is only for fields which require admin access, such as sortId + doi
      throw new IllegalAccessException(s"No permission to patch the abstract")
    }

    val patches = patchReads.reads(request.body).getOrElse {
      Logger.debug("Invalid patch description")
      throw new IllegalArgumentException("Invalid patch description")
    }.map {
      case("add", "/sortId", Some(v: JsNumber)) =>  PatchAddSortId(v.value.toInt)
      case("add", "/doi", Some(v: JsString)) => PatchAddDOI(v.value)
      case _ => throw new IllegalArgumentException("Unsupported patch operation")
    }.toList

    val patched = abstractService.patch(abstr, patches)
    Ok(Json.toJson(patched))
  }
}

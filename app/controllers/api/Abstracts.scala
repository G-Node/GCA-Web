package controllers.api

import org.joda.time.format.DateTimeFormat
import play.api._
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsArray, JsObject, Json, _}
import play.api.mvc._
import service._
import utils.DefaultRoutesResolver._
import utils.GCAAuth
import utils.serializer.{AbstractFormat, AccountFormat, StateLogWrites}
import models._

/**
 * Abstracts controller.
 * Manages HTTP request logic for abstracts.
 */
object Abstracts extends Controller with  GCAAuth {

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
  def create(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>

    val abs = request.body.as[Abstract]
    val conference = conferenceService.get(id)

    if(!conference.isOpen &&
      !(request.user.isAdmin || conference.isOwner(request.user))) {
      throw new IllegalAccessException("Conference is closed!")
    }

    val newAbs = abstractService.create(abs, conference, request.user)

    Created(Json.toJson(newAbs))
  }

  /**
   * List all published abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listByConference(id: String) = AccountAwareAction(isREST = true) {  implicit request =>

    val conference = conferenceService.get(id)
    val abstracts = abstractService.list(conference)

    Ok(Json.toJson(abstracts))
  }

  /**
   * List all abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listAllByConference(id: String) = AuthenticatedAction(isREST = true) {  implicit request =>

    val conference = conferenceService.get(id)

    if (!(request.user.isAdmin || conference.isOwner(request.user))) {
      throw new IllegalAccessException("Not allowed")
    }

    val abstracts = abstractService.listAll(conference)
    Ok(Json.toJson(abstracts))
  }

  /**
   * List all abstracts for a given user.
   *
   * @return All (accessible) abstracts for a given user.
   */
  def listByAccount(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    val ownAbstracts = abstractService.listOwn(request.user)

    Ok(Json.toJson(ownAbstracts))
  }


  /**
   * List all abstracts for a given conference and a given user
   *
   * @return All (accessible) abstracts for a given user.
   */
  def listOwn(conferenceId: String) = AuthenticatedAction(isREST = true) { implicit request =>

  val conference = conferenceService.get(conferenceId)
  val abstracts = abstractService.listOwn(conference, request.user)

  Ok(Json.toJson(abstracts))
}

  /**
   * An abstract info by id.
   *
   * @param id The id of the abstract.
   *
   * @return An abstract as JSON / abstract page.
   */
  def get(id: String) = AccountAwareAction(isREST = true) { implicit request =>
    Logger.debug(s"Getting abstract with uuid: [$id]")

    val abs = request.user match {
      case Some(user) => abstractService.getOwn(id, user)
      case _          => abstractService.get(id)
    }

    Ok(Json.toJson(abs)).withHeaders(LAST_MODIFIED -> rfcDateFormatter.print(abs.mtime))
  }

  /**
   * Update an existing conference info.
   *
   * @param id   The abstract to update.
   *
   * @return abstract in JSON / abstract page
   */
  def update(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    Logger.debug(s"Updating abstract with uuid: [$id]")

    val abs = request.body.as[Abstract]

    if (abs.uuid != null && abs.uuid != id) {
      //TODO: should that be allowed? I guess not - ck
      throw new RuntimeException("Trying to change the id of an abstract!")
      Logger.debug(s"Updating [$id]: UUID mismatch")
    }

    val oldAbstract = abstractService.getOwn(abs.uuid, request.user)
    val conference = oldAbstract.conference

    if(!conference.isOpen && oldAbstract.state != AbstractState.InRevision) {
      throw new IllegalAccessException("Conference is closed and abstract not in 'InRevision' state!")
    }

    val newAbstract = abstractService.update(abs, request.user)

    Ok(Json.toJson(newAbstract))
  }

  /**
   * Delete an existing abstract.
   *
   * @param id   Abstract id to delete.
   *
   * @return OK or Failed / Redirect to the abstract list page
   */
  def delete(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    Logger.debug(s"Deleting abstract with uuid: [$id]")

    abstractService.delete(id, request.user)

    Ok("Abstract Deleted") //FIXME: JSON
  }

  /**
   * Set permissions on the abstract.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def setPermissions(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>

    val to_set = for (acc <- request.body.as[List[JsObject]])
      yield accountFormat.reads(acc).get

    val abstr = abstractService.getOwn(id, request.user)
    val owners = abstractService.setPermissions(abstr, request.user, to_set)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  /**
   * Get permissions of the abstract.
   *
   * @return a list of updated permissions (accounts) as JSON
   */
  def getPermissions(id: String) = AuthenticatedAction(isREST = true) { implicit request =>

    val abstr = abstractService.getOwn(id, request.user)
    val owners = abstractService.getPermissions(abstr, request.user)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  def listState(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    implicit val logWrites = new StateLogWrites()

    Ok(Json.toJson(abstractService.listStates(id, request.user)))
  }


  def setState(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    implicit val logWrites = new StateLogWrites()
    val changeReads = ((__ \ "state").read[String] and (__ \ "note").readNullable[String]).tupled

    val account = request.user
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

  def patch(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    val patchReads = Reads.list(((__ \ "op").read[String] and
      (__ \ "path").read[String] and (__ \ "value").readNullable[JsValue]).tupled)

    val account = request.user
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

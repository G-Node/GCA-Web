package controllers.api

import play.api._
import play.api.mvc._
import service._
import utils.serializer.{StateLogWrites, AccountFormat, AbstractFormat}
import play.api.libs.json.{JsArray, JsObject, Json}
import utils.GCAAuth
import models.{StateLogEntry, AbstractState, Abstract}
import utils.DefaultRoutesResolver._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.functional.syntax._
import service.util.EMPImplicits.EMPFromRequest

/**
 * Abstracts controller.
 * Manages HTTP request logic for abstracts.
 */
object Abstracts extends Controller with  GCAAuth {

  implicit val absFormat = new AbstractFormat()
  val accountFormat = new AccountFormat()

  //RFC 1123
  val rfcDateFormatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC()

  /**
   * Create a new abstract.
   *
   * @return new abstract in JSON / Redirect to the abstract page
   */
  def create(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>


    val conferenceService = ConferenceService()
    val abstractSservice = AbstractService()

    val abs = request.body.as[Abstract]

    val conference = conferenceService.get(id)

    if(!conference.isOpen &&
      !(request.user.isAdmin || conference.isOwner(request.user))) {
      throw new IllegalAccessException("Conference is closed!")
    }

    val newAbs = abstractSservice.create(abs, conference, request.user)

    Created(Json.toJson(newAbs))
  }

  /**
   * List all published abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listByConference(id: String) = AccountAwareAction(isREST = true) {  implicit request =>

    val conferenceService = ConferenceService()
    val abstractService = AbstractService()

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

    val conferenceService = ConferenceService()
    val abstractService = AbstractService()

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
    val abstracts = AbstractService()
    val ownAbstracts = abstracts.listOwn(request.user)
    Ok(Json.toJson(ownAbstracts))
  }


  /**
   * List all abstracts for a given conference and a given user
   *
   * @return All (accessible) abstracts for a given user.
   */
  def listOwn(conferenceId: String) = AuthenticatedAction(isREST = true) { implicit request =>

  val conferenceService = ConferenceService()
  val abstractService = AbstractService()

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

    val abstracts = AbstractService()

    val abs = request.user match {
      case Some(user) => abstracts.getOwn(id, user)
      case _          => abstracts.get(id)
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

    val abstracts = AbstractService()
    val conferenceService = ConferenceService()

    val abs = request.body.as[Abstract]

    if (abs.uuid != null && abs.uuid != id) {
      //TODO: should that be allowed? I guess not - ck
      throw new RuntimeException("Trying to change the id of an abstract!")
      Logger.debug(s"Updating [$id]: UUID mismatch")
    }

    val oldAbstract = abstracts.getOwn(abs.uuid, request.user)
    val conference = oldAbstract.conference

    if(!conference.isOpen && oldAbstract.state != AbstractState.InRevision) {
      throw new IllegalAccessException("Conference is closed and abstract not in 'InRevision' state!")
    }

    val newAbstract = abstracts.update(abs, request.user)

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

    val abstracts = AbstractService()
    abstracts.delete(id, request.user)
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

    val srv = AbstractService()
    val abstr = srv.getOwn(id, request.user)
    val owners = srv.setPermissions(abstr, request.user, to_set)

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

    val srv = AbstractService()
    val abstr = srv.getOwn(id, request.user)
    val owners = srv.getPermissions(abstr, request.user)

    Ok(JsArray(
      for (acc <- owners) yield accountFormat.writes(acc)
    ))
  }

  def listState(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    implicit val logWrites = new StateLogWrites()
    val srv = AbstractService()
    Ok(Json.toJson(srv.listStates(id, request.user)))
  }


  def setState(id: String) = AuthenticatedAction(parse.json, isREST = true) { implicit request =>
    implicit val logWrites = new StateLogWrites()
    val changeReads = ((__ \ "state").read[String] and (__ \ "note").readNullable[String]).tupled

    val account = request.user
    val srv = AbstractService()
    val abstr = srv.getOwn(id, account) // TODO: will not work for admin (GitHub issue, #155)
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

    val stateLog = srv.setState(abstr, toState, account, msg)

    Ok(Json.toJson(stateLog))
  }
}

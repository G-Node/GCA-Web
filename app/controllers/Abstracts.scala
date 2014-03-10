package controllers

import play.api._
import play.api.mvc._
import service._
import utils.serializer.AbstractFormat
import play.api.libs.json.Json
import utils.GCAAuth
import models.{Abstract, Account}


/**
 * Abstracts controller.
 * Manages HTTP request logic for abstracts.
 */
object Abstracts extends Controller with OwnerManager with  GCAAuth {

  implicit val absFormat = new AbstractFormat("http://localhost:9000") //FIXME

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
    val newAbs = abstractSservice.create(abs, conference, request.user)

    Created(Json.toJson(newAbs))
  }

  /**
   * List all abstracts for a given conference.
   *
   * @return All abstracts publicly available.
   */
  def listByConference(id: String) = AccountAwareAction {  implicit request =>

    val conferenceService = ConferenceService()
    val abstractService = AbstractService()

    val conference = conferenceService.get(id)

    val abstracts = abstractService.list(conference)

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

    Ok(Json.toJson(abs))
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

    val abs = request.body.as[Abstract]

    if (abs.uuid != null && abs.uuid != id) {
      //TODO: should that be allowed? I guess not - ck
      throw new RuntimeException("Trying to change the id of an abstract!")
      Logger.debug(s"Updating [$id]: UUID mismatch")
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
}

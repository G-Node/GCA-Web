package controllers

import play.api.mvc._
import play.api.libs.json._

import java.util.{List => JList, LinkedList => JLinkedList}

import service.ConferenceService
import models.Conference
import models.{Abstract => Abstr}

/**
 * Conferences controller.
 * Manages HTTP request logic for conferences.
 */

object Conferences extends Controller {

  /**
   * Create a new conference.
   *
   * @return new conference in JSON / Redirect to the conference page
   */
  def create = Action(parse.json) { request =>
    val confService = new ConferenceService()
    val json = request.body
    confReads.reads(json).fold(
      valid = name => {
        val resp = confService.create(Conference(null, name), request.user)
        Created(confWrites.writes(resp))
      },
      invalid = e => BadRequest(e.toString)
    )
  }

  /**
   * List all available conferences.
   *
   * @return All conferences publicly available.
   */
  def list: Action[AnyContent] = Action { implicit request =>
    render {
      case Accepts.Html() => Ok(views.html.index("Your new application is ready."))
      case Accepts.Json() => Ok(Json.toJson("Your new application is ready.").toString())
    }
  }

  /**
   * A conference info by id.
   *
   * @param id The id of the conference.
   *
   * @return A conference as JSON / page with conference info.
   */
  def get(id: String) : Action[AnyContent] = TODO

  /**
   * Update an existing conference info.
   *
   * @param id   The conference id to update.
   *
   * @return conference in JSON / conference page
   */
  def update(id: String) : Action[AnyContent] = TODO

  /**
   * Delete an existing conference.
   *
   * @param id   Conference id to delete.
   *
   * @return OK or Failed / Redirect to the conferences list page
   */
  def delete(id: String) : Action[AnyContent] = TODO

  val confReads = (__ \ "name").read[String]

  val confWrites = new Writes[Conference] {
    def writes(c: Conference): JsValue = {
      Json.obj(
        "name" -> c.name,
        "uuid" -> c.uuid,
        "abstracts" -> "todo"
      )
    }
  }
}


package controllers.api

import play.api._
import play.api.libs.json.{JsArray, _}
import play.api.mvc._
import service.{AbstractService, FigureService, FigureMobileService}
import utils.DefaultRoutesResolver._
import utils.serializer.FigureFormat
import models._

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{Silhouette, Environment}

import scala.collection.JavaConversions._

/**
 * Figures controller.
 * Manages HTTP request logic for upload/download figures for abstracts.
 */
class Figures(implicit val env: Environment[Login, CachedCookieAuthenticator])
extends Silhouette[Login, CachedCookieAuthenticator] {

  implicit val figFormat = new FigureFormat()
  val abstractService = AbstractService()
  val figureService = FigureService()
  val figureMobileService = FigureMobileService()

  /**
   * Upload file with a figure to the specified abstract (id).
   * An incoming request Content-Type should be multipart/form-data.
   * Request should contain minimum two parts:
   * - "file" with associated File data
   * - "figure" with associated JSON string with Figure data
   *
   * @param id  The id of the abstract.
   *
   * @return  OK / Failed
   */
  def upload(id: String) = SecuredAction(parse.multipartFormData) { implicit request =>
    val abstr = abstractService.getOwn(id, request.identity.account)
    val tempfile = request.body.file("file").map {
      figure => figure.ref
    }.getOrElse {
      throw new IllegalArgumentException("File is missing")
    }

    val jsfig = Json.parse(request.body.dataParts("figure")(0)).as[Figure]

    jsfig.position = abstr.figures.toList.map(_.position).reduceLeftOption(_ max _).getOrElse(0) + 1

    val figure = figureService.create(jsfig, tempfile, abstr, request.identity.account)

    Created(figFormat.writes(figure))
  }

  /**
   * Download figure from the specified abstract (id).
   *
   * @param id  The id of the abstract.
   *
   * @return  OK / Failed
   */
  def list(id: String) = UserAwareAction { implicit request =>
    Ok(JsArray(
      for (fig <- asScalaSet(
          abstractService.get(id).figures
        ).toSeq
      ) yield figFormat.writes(fig)
    ))
  }

  /**
   * Download figure file from the specified figure object (id).
   *
   * @param id  The id of the figure.
   *
   * @return  OK / Failed
   */
  def download(id: String) = UserAwareAction { implicit request =>
    Ok.sendFile(figureService.openFile(
      figureService.get(id)
    ))
  }

  /**
   * Delete an existing figure (id).
   *
   * @param id   The id of the figure.
   *
   * @return  OK / Failed
   */
  def delete(id: String) = SecuredAction { implicit request =>
    figureService.delete(id, request.identity.account)
    Ok(Json.obj("error" -> false))
  }

  /**
   * Update an existing figure (id).
   *
   * @param id   The id of the figure.
   *
   * @return  OK / Failed
   */

  def updateFigure(id: String) = SecuredAction(parse.json) { implicit request =>

    val figure = request.body.as[Figure]
    val oldFig = figureService.get(figure.uuid)

    // get abstractID and position from database
    figure.abstr = abstractService.getOwn(oldFig.abstr.uuid, request.identity.account)
    figure.position = oldFig.position

    figureService.update(figure, request.identity.account)
    Ok(Json.obj("error" -> false))
  }

  /**
    * Download mobile figure file from the specified figure object (id).
    *
    * @param id  The id of the figure.
    *
    * @return  OK / Failed
    */
  def downloadmobile(id: String) = UserAwareAction { implicit request =>
    Ok.sendFile(figureMobileService.openFile(
      figureMobileService.get(id)
    ))
  }
}

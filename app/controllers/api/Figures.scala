package controllers.api

import play.api.mvc._
import service.{FigureService, AbstractService}
import models.Figure
import utils.GCAAuth
import utils.serializer.FigureFormat
import play.api.libs.json._
import scala.collection.JavaConversions._
import play.api.libs.json.JsArray
import utils.DefaultRoutesResolver._

/**
 * Figures controller.
 * Manages HTTP request logic for upload/download figures for abstracts.
 */
object Figures extends Controller with GCAAuth {

  implicit val figFormat = new FigureFormat()

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
  def upload(id: String) = AuthenticatedAction(parse.multipartFormData, isREST = true) { implicit request =>
    val abstr = AbstractService().getOwn(id, request.user)
    val tempfile = request.body.file("file").map {
      figure => figure.ref
    }.getOrElse {
      throw new IllegalArgumentException("File is missing")
    }

    val jsfig = Json.parse(request.body.dataParts("figure")(0)).as[Figure]
    val figure = FigureService().create(jsfig, tempfile, abstr, request.user)

    Created(figFormat.writes(figure))
  }

  /**
   * Download figure from the specified abstract (id).
   *
   * @param id  The id of the abstract.
   *
   * @return  OK / Failed
   */
  def list(id: String) = AccountAwareAction { request =>
    Ok(JsArray(
      for (fig <- asScalaSet(
          AbstractService().get(id).figures
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
  def download(id: String) = AccountAwareAction { request =>
    Ok.sendFile(FigureService().openFile(
      FigureService().get(id)
    ))
  }

  /**
   * Delete an existing figure (id).
   *
   * @param id   The id of the figure.
   *
   * @return  OK / Failed
   */
  def delete(id: String) = AuthenticatedAction(isREST = true) { implicit request =>
    FigureService().delete(id, request.user)
    Ok(Json.obj("error" -> false))
  }

}

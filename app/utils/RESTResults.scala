package utils

import play.api.mvc.Results
import play.api.libs.json._
import play.api.libs.json.JsString
import play.api.libs.json.JsBoolean
import play.api.data.validation.ValidationError

/**
 * Extension for the basic HTTP responses for the REST API.
 * Basically a wrapper over default HTTP responses to return results in JSON.
 */
object RESTResults extends Results {

  val JSONValidationError = {e: (Seq[(JsPath, Seq[ValidationError])]) =>
    BadRequest(Json.obj(
      "error" -> JsBoolean(true),
      "causes" -> JsError.toFlatJson(e))
    )
  }

  val UserNotFound = NotFound(Json.obj(
    "error" -> JsBoolean(true),
    "causes" -> Json.obj(
      "user" -> JsString("Account not found")
    )))

  val ObjectNotFound = NotFound(Json.obj(
    "error" -> JsBoolean(true),
    "causes" -> Json.obj(
      "id" -> JsString("Object not found")
    )))

  val AccessForbidden = Forbidden(Json.obj(
    "error" -> JsBoolean(true),
    "causes" -> Json.obj(
      "id" -> JsString("Access to this object is forbidden")
    )))

  val Success = {js: JsValue =>
    Ok(Json.obj(
      "error" -> JsBoolean(false),
      "objects" -> js
    ))
  }

  val Deleted = Ok(Json.obj(
      "error" -> JsBoolean(false)
    ))
}
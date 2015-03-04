package forms

import play.api.data.Form
import play.api.data.Forms._


object EmailForm {

  val emailForm = Form(single("email" -> email))

}

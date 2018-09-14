package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages


object ResetPasswordForm {

  val passwordsForm = Form(tuple(
    "passwordold" -> nonEmptyText,
    "password1" -> nonEmptyText(minLength = 6),
    "password2" -> nonEmptyText
  ) verifying(Messages("passwords.not.equal"), passwords => passwords._2 == passwords._3 ))

}

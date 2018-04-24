package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages


object NewMailForm {

  val newmailform = Form(tuple(
    "email1" -> nonEmptyText(minLength = 6),
    "email2" -> nonEmptyText(minLength = 6),
    "password" -> nonEmptyText
  ) verifying(Messages("passwords.not.equal"), mails => mails._2 == mails._1 ))

}

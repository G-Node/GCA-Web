package service.mail

import models.Account
import play.api.Play.current
import play.api.libs.mailer.{MailerPlugin, Email}


class MailerService {

  val from: String = current.configuration.getString("smtp.from").get

  def sendPasswordReset(account: Account, password: String, loginUrl: String): Unit = {
    val mail = Email(
      subject = "Reset password",
      from = from,
      to = Seq(account.mail),
      bodyText = Some(views.html.mail.pwreset(account, password, loginUrl).toString())
    )

    MailerPlugin.send(mail)
  }

  def sendConfirmation(account: Account, tokenUrl: String): Unit = {
    val mail = Email(
      subject = "Please confirm your registration",
      from = from,
      to = Seq(account.mail),
      bodyText = Some(views.html.mail.confirmation(account, tokenUrl).toString())
    )

    MailerPlugin.send(mail)
  }

}

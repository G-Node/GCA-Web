package service.mail

import models.Account
import play.api.Play.current
import com.typesafe.plugin._


class MailerService {

  val mailer: MailerPlugin = use[MailerPlugin]
  val from: String = current.configuration.getString("smtp.from").get

  def sendPasswordReset(account: Account, password: String, loginUrl: String): Unit = {
    val mail = mailer.email

    mail.setFrom(from)
    mail.setRecipient(account.mail)
    mail.setSubject("Please confirm your registration")
    mail.send(views.html.mail.pwreset(account, password, loginUrl).toString())
  }

  def sendConfirmation(account: Account, tokenUrl: String): Unit = {
    val mail = mailer.email

    mail.setFrom(from)
    mail.setRecipient(account.mail)
    mail.setSubject("Please confirm your registration")
    mail.send(views.html.mail.confirmation(account, tokenUrl).toString())
  }

}

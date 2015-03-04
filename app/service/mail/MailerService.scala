package service.mail

import models.Account
import play.api.Play.current
import com.typesafe.plugin._


class MailerService {

  val mailer: MailerPlugin = {
    if(!play.api.Play.isProd)
      new MockMailerPlugin
    else
      use[MailerPlugin]
  }

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


class MockMailerPlugin extends MailerPlugin {
  def email: MailerAPI = new MockMailerAPI
}


class MockMailerAPI extends MailerAPI {
  private val context = collection.mutable.Map[String, List[String]]()

  def setSubject(subject: String, args: AnyRef*): MailerAPI = {
    context += ("subject" -> List(String.format(subject, args: _*)))
    this
  }

  def setSubject(subject: String): MailerAPI = {
    context += ("subject" -> List(subject))
    this
  }

  def setFrom(from: String): MailerAPI = {
    context += ("from" -> List(from))
    this
  }

  def setReplyTo(replyTo: String): MailerAPI = {
    context += ("replyTo" -> List(replyTo))
    this
  }

  def setCharset(charset: String): MailerAPI = {
    context += ("charset" -> List(charset))
    this
  }

  def addHeader(key: String, value: String): MailerAPI = {
    context += ("header-" + key -> List(value))
    this
  }

  def setCc(ccRecipients: String*): MailerAPI = {
    context += ("ccRecipients" -> ccRecipients.toList)
    this
  }

  def setBcc(bccRecipients: String*): MailerAPI = {
    this
  }

  def setRecipient(recipients: String*): MailerAPI = {
    this
  }


  override def send(bodyText: String) {
    println(context)
    println(bodyText)
  }

  override def send(bodyText: String, bodyHtml: String) {
    println(context)
    println(bodyText)
    println(bodyHtml)
  }

  override def sendHtml(bodyHtml: String) {
    println(context)
    println(bodyHtml)
  }
}


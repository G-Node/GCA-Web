// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import java.util.{Set => JSet, TreeSet => JTreeSet}
import javax.persistence._

import com.mohiva.play.silhouette.core.providers.PasswordInfo
import com.mohiva.play.silhouette.core.{Identity, LoginInfo}
import models.Model._
import models.util.DateTimeConverter
import org.joda.time.{DateTime, LocalDateTime}
import play.api.Play.current

/**
 * Model class for accounts.
 */
@Entity
class Account extends Model {

  @Column(unique = true)
  var mail: String = _
  var firstName: String = _
  var lastName: String = _
  var fullName: String = _

  var avatar: String = _

  @Convert(converter = classOf[DateTimeConverter])
  var ctime: DateTime = _
  @Convert(converter = classOf[DateTimeConverter])
  var mtime: DateTime = _

  @ManyToMany(mappedBy = "owners")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  /**
    * here ManyToMany, to get number of likes/author access to liking persons
    */
  @ManyToMany(mappedBy = "favUsers")
  var favAbstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @ManyToMany(mappedBy = "owners")
  var conferences: JSet[Conference] = new JTreeSet[Conference]()

  @OneToMany(mappedBy = "account", cascade = Array(CascadeType.ALL))
  var logins: JSet[CredentialsLogin] = new JTreeSet[CredentialsLogin]()

  def isAdmin = {
    val admins = current.configuration.getStringList("admins").get
    admins.contains(mail)
  }

}

object Account {

  def apply(uuid: Option[String],
            mail: Option[String],
            abstracts: Seq[Abstract] = Nil,
            favAbstracts: Seq[Abstract] = Nil,
            conferences: Seq[Conference] = Nil) : Account = {

    val account = new Account()

    account.uuid        = unwrapRef(uuid)
    account.mail        = unwrapRef(mail)

    account.conferences = toJSet(conferences)
    account.abstracts   = toJSet(abstracts)
    account.favAbstracts   = toJSet(favAbstracts)

    account
  }

  def apply(uuid: String,
            mail: String,
            firstName: String,
            lastName: String,
            fullName: Option[String]) : Account = {

    val account = new Account()

    account.uuid        = uuid
    account.mail        = mail
    account.firstName   = firstName
    account.lastName    = lastName
    account.fullName = fullName match {
      case Some(name) => name
      case _ => s"$firstName $lastName"
    }

    account.conferences = toJSet(Nil)
    account.abstracts   = toJSet(Nil)
    account.favAbstracts   = toJSet(Nil)

    account
  }

}


@MappedSuperclass
abstract class Login extends Model with Identity {

  @ManyToOne
  var account: Account = _
}


@Entity
class CredentialsLogin extends Login {

  var hasher: String = _
  var password: String = _
  var salt: String = _
  var isActive: Boolean = _
  var token: String = _
  var date: LocalDateTime = _


  override def loginInfo: LoginInfo = new LoginInfo("credentials", account.mail)

  @PrePersist
  override protected def beforePersist(): Unit = {
    super.beforePersist()
    if (date == null) {
      date = LocalDateTime.now()
    }
  }
}


object CredentialsLogin {

  def apply(passwordInfo: PasswordInfo, isActive: Boolean, account: Account): CredentialsLogin = {
    val login = new CredentialsLogin()

    login.hasher = passwordInfo.hasher
    login.password = passwordInfo.password
    login.salt = unwrapRef(passwordInfo.salt)

    login.isActive = isActive
    login.account = account

    login
  }

  def apply(passwordInfo: PasswordInfo, isActive: Boolean, token: String, account: Account): CredentialsLogin = {
    val login = CredentialsLogin(passwordInfo, isActive = isActive, account)

    login.token = token

    login
  }

}
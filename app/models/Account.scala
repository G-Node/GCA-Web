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
import play.api.Play.current

/**
 * Model class for accounts.
 */
@Entity
class Account extends Model {

  var mail: String = _
  var firstName: String = _
  var lastName: String = _
  var fullName: String = _

  var avatar: String = _

  @ManyToMany(mappedBy = "owners")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @ManyToMany(mappedBy = "owners")
  var conferences: JSet[Conference] = new JTreeSet[Conference]()

  @OneToMany(mappedBy = "account", cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  var logins: JSet[Login] = new JTreeSet[Login]()

  def isAdmin = {
    val admins = current.configuration.getStringList("admins").get
    admins.contains(mail)
  }

}

object Account {

  def apply(uuid: Option[String],
            mail: Option[String],
            abstracts: Seq[Abstract] = Nil,
            conferences: Seq[Conference] = Nil) : Account = {

    val account = new Account()

    account.uuid        = unwrapRef(uuid)
    account.mail        = unwrapRef(mail)

    account.conferences = toJSet(conferences)
    account.abstracts   = toJSet(abstracts)

    account
  }

}


@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
abstract class Login extends Model with Identity {

  @ManyToOne
  var account: Account = _
}


@Entity
class CredentialsLogin extends Login {

  var hasher: String = _
  var password: String = _
  var salt: String = _

  override def loginInfo: LoginInfo = new LoginInfo("credentials", account.mail)

}


object CredentialsLogin {

  def apply(passwordInfo: PasswordInfo, account: Option[Account]): CredentialsLogin = {
    val login = new CredentialsLogin()

    login.account = unwrapRef(account)
    login.hasher = passwordInfo.hasher
    login.password = passwordInfo.password
    login.salt = unwrapRef(passwordInfo.salt)

    login
  }

}
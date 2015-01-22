// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import com.mohiva.play.silhouette.core.providers.CredentialsProvider
import play.api.Play.current
import models.Model._
import java.util.{Set => JSet, TreeSet => JTreeSet}
import javax.persistence._
import com.mohiva.play.silhouette.core.{LoginInfo, Identity}

@Embeddable
class PwInfo {
  var hasher: String = _
  var password: String = _
  var salt: String = _
}

object PwInfo {

  def apply(h: String,
            pw: String,
            s: Option[String]) : PwInfo = {
    val info: PwInfo = new PwInfo()

    info.hasher = h
    info.password = pw
    info.salt = s match {
      case Some(saltz) => saltz
      case _           => null
    }

    info
  }
}

@Embeddable
class OpenAuth1Info {
  var token: String = _
  var secret: String = _
}

object OpenAuth1Info {
  def apply(t: String, s: String) : OpenAuth1Info = {
    val info: OpenAuth1Info = new OpenAuth1Info()
    info.token  = t
    info.secret = s

    info
  }
}

@Embeddable
class OpenAuth2Info {
  var accessToken: String = _
  var tokenType: String = _
  var expiresIn: Integer = _
  var refreshToken: String = _
}

object OpenAuth2Info {
  def apply(accessToken: String, tokenType: Option[String],
            expiresIn: Option[Int], refreshToken: Option[String]) : OpenAuth2Info = {
    val info: OpenAuth2Info = new OpenAuth2Info()
    info.accessToken  = accessToken
    info.tokenType    = Model.unwrapRef(tokenType)
    info.expiresIn    = expiresIn match { case Some(i) => i; case _ => null }
    info.refreshToken = Model.unwrapRef(refreshToken)

    info
  }

}

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

  @OneToMany(cascade = Array(CascadeType.ALL), fetch = FetchType.LAZY)
  @JoinColumn(name= "account_uuid")
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
@Table(name="Login")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="TYPE", discriminatorType=DiscriminatorType.STRING,length=20)
@DiscriminatorValue("Base")
abstract class Login extends Model with Identity {

  @ManyToOne
  var account: Account = _
}


@Entity
@DiscriminatorValue("Credentials")
class CredentialsLogin extends Login {

  override def loginInfo: LoginInfo = new LoginInfo("credentials", account.mail)

}
// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import play.api.Play.current
import models.Model._
import java.util.{Set => JSet, TreeSet => JTreeSet}
import javax.persistence.{Embedded, Embeddable, ManyToMany, Entity}
import securesocial.core._
import securesocial.core.{IdentityId, OAuth2Info, OAuth1Info}


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
class Account extends Model with Identity {

  var mail: String = _
  var firstName: String = _
  var lastName: String = _
  var fullName: String = _

  var avatar: String = _

  //IdendityId class
  var provider: String = _
  var userid: String = _

  //AuthenticationMethod class
  var authenticationMethod: String = _

  @Embedded
  var pwInfo: PwInfo = _

  @Embedded
  var oa1Info: OpenAuth1Info = _

  @Embedded
  var oa2Info: OpenAuth2Info = _

  @ManyToMany(mappedBy = "owners")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @ManyToMany(mappedBy = "owners")
  var conferences: JSet[Conference] = new JTreeSet[Conference]()


  def isAdmin = {
    val admins = current.configuration.getStringList("admins").get
    admins.contains(userid)
  }

  //Identity specific getter

  override def identityId: IdentityId = {
    new IdentityId(userid, provider)
  }


  override def email: Option[String] = {
    mail match {
      case a: String => Some(mail)
      case _         => None
    }
  }

  override def avatarUrl: Option[String] = {
    avatar match {
      case s: String => Some(s)
      case _         => None
    }
  }

  override def authMethod: AuthenticationMethod = {
    new AuthenticationMethod(authenticationMethod)
  }

  override def oAuth1Info: Option[OAuth1Info]  = {
    if (oa1Info != null) {
      Some(new OAuth1Info(oa1Info.token, oa1Info.secret))
    } else {
      None
    }
  }

  override def oAuth2Info: Option[OAuth2Info] = {
    if (oa2Info != null) {
      val tokenType    = oa2Info.tokenType match { case s: String => Some(s); case _ => None }
      val expiresIn    = oa2Info.expiresIn match { case i: Integer => Some(i.asInstanceOf[Int]); case _ => None }
      val refreshToken = oa2Info.refreshToken match { case s: String => Some(s); case _ => None }

      Some(new OAuth2Info(oa2Info.accessToken, tokenType, expiresIn, refreshToken))
    } else {
      None
    }
  }


  override def passwordInfo: Option[PasswordInfo] = {
    if (pwInfo != null) {
      val salt = pwInfo.salt match {
        case s: String => Some(s)
        case _         => None
      }

      Some(new PasswordInfo(pwInfo.hasher, pwInfo.password, salt))
    } else {
      None
    }
  }

  def updateFromIdentity(id: Identity) {
    this.authenticationMethod = id.authMethod.method
    this.userid    = id.identityId.userId
    this.provider  = id.identityId.providerId
    this.firstName = id.firstName
    this.lastName  = id.lastName
    this.fullName  = id.fullName
    this.mail      = unwrapRef(id.email)
    this.avatar    = unwrapRef(id.avatarUrl)

    this.pwInfo    = id.passwordInfo match {
      case Some(i) => PwInfo(i.hasher, i.password, i.salt)
      case _       => null
    }

    this.oa1Info = id.oAuth1Info match {
      case Some(i) => OpenAuth1Info(i.token, i.secret)
      case _       => null
    }

    this.oa2Info = id.oAuth2Info match {
      case Some(i) => OpenAuth2Info(i.accessToken, i.tokenType, i.expiresIn, i.refreshToken)
      case _       => null
    }

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

  def apply(id: Identity) : Account = {
    val account = new Account()
    account.updateFromIdentity(id)
    account
  }

}

// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import models.Model._
import java.util.{Set => JSet, TreeSet => JTreeSet}
import javax.persistence.{Embedded, Embeddable, ManyToMany, Entity}
import securesocial.core._
import securesocial.core.OAuth2Info
import securesocial.core.OAuth1Info
import securesocial.core.IdentityId
import scala.beans.BeanProperty

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


/**
 * Model class for accounts.
 */
@Entity
class Account extends Model with Identity {

  var mail: String = _
  var firstName: String = _
  var lastName: String = _
  var fullName: String = _

  //IdendityId class
  var provider: String = _
  var userid: String = _

  //AuthenticationMethod class
  var authenticationMethod: String = _

  @Embedded
  var pwInfo: PwInfo = _

  @ManyToMany(mappedBy = "owners")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @ManyToMany(mappedBy = "owners")
  var conferences: JSet[Conference] = new JTreeSet[Conference]()


  //Identity

  override def identityId: IdentityId = {
    new IdentityId(userid, provider)
  }


  override def email: Option[String] = {
    mail match {
      case a: String => Some(mail)
      case _         => None
    }
  }

  override def avatarUrl: Option[String] = { None }

  override def authMethod: AuthenticationMethod = {
    new AuthenticationMethod(authenticationMethod)
  }

  override def oAuth1Info: Option[OAuth1Info]  = { null }
  override def oAuth2Info: Option[OAuth2Info] = { null }
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

    this.pwInfo    = id.passwordInfo match {
      case Some(i) => PwInfo(i.hasher, i.password, i.salt)
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

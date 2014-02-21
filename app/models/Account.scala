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
import javax.persistence.{ManyToMany, Entity}

/**
 * Model class for accounts.
 */
@Entity
class Account extends Model {

  var mail: String = _

  @ManyToMany(mappedBy = "owners")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @ManyToMany(mappedBy = "owners")
  var conferences: JSet[Conference] = new JTreeSet[Conference]()

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

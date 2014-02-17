// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import java.util.{List => JList, LinkedList => JLinkedList}

/**
 * Model class for accounts.
 */
class Account extends Model {

  var mail: String = _
  var abstracts: JList[Abstract] = new JLinkedList[Abstract]()

}

object Account {

  def apply() : Account = new Account()

  def apply(uuid: String,
            mail: String,
            abstracts: JList[Abstract] = null) : Account = {

    val account = new Account()

    account.uuid = uuid
    account.mail = mail

    if (abstracts != null)
      account.abstracts = abstracts

    account
  }

}

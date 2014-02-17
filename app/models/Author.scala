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
 * Model class for abstract authors
 */
class Author extends Model {

  var mail: String = _
  var firstName: String = _
  var middleName: String = _
  var lastName: String = _

  var abstr: Abstract = _
  var affiliations: JList[Affiliation] = new JLinkedList[Affiliation]()

}


object Author {

  def apply() : Author = new Author()

  def apply(uuid: String,
            mail: String,
            firstName: String,
            middleName: String,
            lastName: String,
            abstr: Abstract,
            affiliations: JList[Affiliation] = null) : Author = {

    val author = new Author()

    author.uuid = uuid
    author.mail = mail
    author.firstName = firstName
    author.middleName = middleName
    author.lastName = lastName
    author.abstr = abstr

    if (affiliations != null)
      author.affiliations = affiliations

    author
  }

}

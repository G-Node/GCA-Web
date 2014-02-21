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
import javax.persistence.{JoinTable, ManyToMany, ManyToOne, Entity}

/**
 * Model class for abstract authors
 */
@Entity
class Author extends Model {

  var mail: String = _
  var firstName: String = _
  var middleName: String = _
  var lastName: String = _

  @ManyToOne
  var abstr: Abstract = _
  @ManyToMany
  @JoinTable(name = "author_affiliations")
  var affiliations: JSet[Affiliation] = new JTreeSet[Affiliation]()

}


object Author {

  def apply(uuid: Option[String],
            mail: Option[String],
            firstName: Option[String],
            middleName: Option[String],
            lastName: Option[String],
            abstr: Option[Abstract] = None,
            affiliations: Seq[Affiliation] = Nil) : Author = {

    val author = new Author()

    author.uuid         = unwrapRef(uuid)
    author.mail         = unwrapRef(mail)
    author.firstName    = unwrapRef(firstName)
    author.middleName   = unwrapRef(middleName)
    author.lastName     = unwrapRef(lastName)

    author.abstr        = unwrapRef(abstr)
    author.affiliations = toJSet(affiliations)

    author
  }

}

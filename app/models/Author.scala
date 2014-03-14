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
import javax.persistence._

/**
 * Model class for abstract authors
 */
@Entity
class Author extends Model {

  var mail: String = _
  var firstName: String = _
  var middleName: String = _
  var lastName: String = _
  var position: Int = _

  @ManyToOne
  var abstr: Abstract = _
  @ManyToMany
  @JoinTable(name = "author_affiliations")
  var affiliations: JSet[Affiliation] = new JTreeSet[Affiliation]()

  @Transient
  var affiliationPositions: Seq[Int] = Nil

}


object Author {

  def apply(uuid: Option[String],
            mail: Option[String],
            firstName: Option[String],
            middleName: Option[String],
            lastName: Option[String],
            position: Option[Int],
            abstr: Option[Abstract] = None,
            affiliations: Seq[Affiliation] = Nil,
            affiliationPositions: Seq[Int] = Nil) : Author = {

    val author = new Author()

    author.uuid         = unwrapRef(uuid)
    author.mail         = unwrapRef(mail)
    author.firstName    = unwrapRef(firstName)
    author.middleName   = unwrapRef(middleName)
    author.lastName     = unwrapRef(lastName)
    author.position     = unwrapVal(position)

    author.abstr        = unwrapRef(abstr)
    author.affiliations = toJSet(affiliations)
    author.affiliationPositions = affiliationPositions

    author
  }

}

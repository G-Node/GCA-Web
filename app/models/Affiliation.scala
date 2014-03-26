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
import javax.persistence.{ManyToOne, ManyToMany, Entity}

/**
 * Model for affiliation
 */
@Entity
class Affiliation extends PositionedModel {

  var address: String = _
  var country: String = _
  var department: String = _
  var name: String = _
  var section: String = _

  @ManyToOne
  var abstr: Abstract = _
  @ManyToMany(mappedBy = "affiliations")
  var authors: JSet[Author] = new JTreeSet[Author]()

}


object Affiliation {

  def apply(uuid: Option[String],
            address: Option[String],
            country: Option[String],
            department: Option[String],
            name: Option[String],
            section: Option[String],
            abstr: Option[Abstract] = None,
            authors: List[Author] = Nil) : Affiliation = {

    val affiliation = new Affiliation()

    affiliation.uuid        = unwrapRef(uuid)
    affiliation.address     = unwrapRef(address)
    affiliation.country     = unwrapRef(country)
    affiliation.department  = unwrapRef(department)
    affiliation.name        = unwrapRef(name)
    affiliation.section     = unwrapRef(section)

    affiliation.abstr       = unwrapRef(abstr)
    affiliation.authors     = toJSet(authors)

    affiliation
  }

}

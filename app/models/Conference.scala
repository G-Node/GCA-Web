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
import javax.persistence.{JoinTable, OneToMany, Entity}

/**
 * A model for that represents a conference.
 *
 * Comment: there could be allot more information about a conference, but
 * maybe we should keep it simple for now.
 */
@Entity
class Conference extends Model {

  var name: String = _

  @OneToMany
  @JoinTable(name = "conference_owners")
  var owners: JSet[Account] = new JTreeSet[Account]()
  @OneToMany(mappedBy = "conference")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()

}

object Conference {

  def apply(uuid: Option[String],
            name: Option[String],
            owners: Seq[Account] = Nil,
            abstracts: Seq[Abstract] = Nil) : Conference = {

    val conference = new Conference()

    conference.uuid       = unwrapRef(uuid)
    conference.name       = unwrapRef(name)

    conference.owners     = toJSet(owners)
    conference.abstracts  = toJSet(abstracts)

    conference
  }

}

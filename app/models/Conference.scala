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
import javax.persistence.{ManyToOne, OneToMany, JoinTable, Entity}

@Entity
class AbstractGroup extends Model {
  var prefix: Int = 0
  var name: String = _
  var short: String = _

  @ManyToOne
  var conference: Conference = _
}

object AbstractGroup {

  def apply(uuid: Option[String],
             prefix: Option[Int],
             name: Option[String],
             short: Option[String]) = {

    val group = new AbstractGroup()
    group.uuid = unwrapRef(uuid)
    group.prefix = prefix match { case Some(i) => i; case _ => -1 }
    group.name = unwrapRef(name)
    group.short = unwrapRef(short)

    group
  }

}

/**
 * A model for that represents a conference.
 *
 * Comment: there could be allot more information about a conference, but
 * maybe we should keep it simple for now.
 */
@Entity
class Conference extends Model {

  var name: String = _
  var short: String = _
  var cite: String = _
  var link: String = _

  var isOpen: Boolean = _


  @OneToMany(mappedBy = "conference")
  var groups: JSet[AbstractGroup] = new JTreeSet[AbstractGroup]()

  @OneToMany
  @JoinTable(name = "conference_owners")
  var owners: JSet[Account] = new JTreeSet[Account]()
  @OneToMany(mappedBy = "conference")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()

}

object Conference {

  def apply(uuid: Option[String],
            name: Option[String],
            short: Option[String],
            cite: Option[String],
            link: Option[String],
            isOpen: Option[Boolean],
            groups: Seq[AbstractGroup] = Nil,
            owners: Seq[Account] = Nil,
            abstracts: Seq[Abstract] = Nil) : Conference = {

    val conference = new Conference()

    conference.uuid       = unwrapRef(uuid)
    conference.name       = unwrapRef(name)
    conference.short      = unwrapRef(short)
    conference.cite       = unwrapRef(cite)
    conference.link       = unwrapRef(link)
    conference.isOpen     = isOpen match {case Some(b) => b; case _ => false}

    conference.groups     = toJSet(groups)
    conference.owners     = toJSet(owners)
    conference.abstracts  = toJSet(abstracts)

    conference
  }

}

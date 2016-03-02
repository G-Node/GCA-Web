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
import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import scala.Some
import models.util.DateTimeConverter
import scala.collection.JavaConversions._
import scala.Some



/**
 * A model for that represents a conference.
 *
 * Comment: there could be allot more information about a conference, but
 * maybe we should keep it simple for now.
 */
@Entity
class Conference extends Model with Owned {

  def dateFormatter = DateTimeFormat.forPattern("d MMMM, yyyy")

  @Column(nullable = false)
  var name: String = _

  @Column(unique = true)
  var short: String = _
  var cite: String = _
  var link: String = _

  var description: String = _

  var isOpen: Boolean = _
  var isPublished: Boolean = _

  @Convert(converter = classOf[DateTimeConverter])
  var startDate: DateTime = _

  @Convert(converter = classOf[DateTimeConverter])
  var endDate: DateTime = _

  @Convert(converter = classOf[DateTimeConverter])
  var deadline: DateTime = null

  var logo: String = _
  var thumbnail: String = _

  var iOSApp: String = _

  @OneToMany(mappedBy = "conference", cascade = Array(CascadeType.ALL), orphanRemoval = true)
  var groups: JSet[AbstractGroup] = new JTreeSet[AbstractGroup]()

  @OneToMany
  @JoinTable(name = "conference_owners")
  var owners: JSet[Account] = new JTreeSet[Account]()
  @OneToMany(mappedBy = "conference")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @OneToMany(mappedBy = "conference", cascade = Array(CascadeType.ALL), orphanRemoval = true)
  var topics: JSet[Topic] = new JTreeSet[Topic]()
}

object Conference {

  def apply(uuid: Option[String],
            name: Option[String],
            short: Option[String],
            cite: Option[String],
            link: Option[String],
            description: Option[String],
            isOpen: Option[Boolean],
            isPublished: Option[Boolean],
            startDate: Option[DateTime],
            endDate: Option[DateTime],
            deadline: Option[DateTime] = null,
            logo: Option[String] = null,
            thumbnail: Option[String] = null,
            iOSApp: Option[String] = null,
            groups: Seq[AbstractGroup] = Nil,
            owners: Seq[Account] = Nil,
            abstracts: Seq[Abstract] = Nil,
            topics: Seq[Topic] = Nil): Conference = {

    val conference = new Conference()

    conference.uuid        = unwrapRef(uuid)
    conference.name        = unwrapRef(name)
    conference.short       = unwrapRef(short)
    conference.cite        = unwrapRef(cite)
    conference.link        = unwrapRef(link)
    conference.description = unwrapRef(description)
    conference.isOpen      = isOpen match {case Some(b) => b; case _ => false}
    conference.isPublished = isPublished match {case Some(b) => b; case _ => false}
    conference.startDate   = unwrapRef(startDate)
    conference.endDate     = unwrapRef(endDate)
    conference.deadline    = unwrapRef(deadline)

    conference.logo        = unwrapRef(logo)
    conference.thumbnail   = unwrapRef(thumbnail)

    conference.iOSApp      = unwrapRef(iOSApp)

    conference.groups      = toJSet(groups)
    conference.owners      = toJSet(owners)
    conference.abstracts   = toJSet(abstracts)
    conference.topics      = toJSet(topics)

    conference
  }

}

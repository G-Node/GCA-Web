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
import org.joda.time.format.DateTimeFormat
import models.util.DateTimeConverter


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

  @Column(nullable = false, unique = true)
  var short: String = _
  @Column(name="conferenceGroup")
  var group: String = _
  var cite: String = _
  var link: String = _

  @Column(length=512)
  var description: String = _

  var isOpen: Boolean = _
  var isPublished: Boolean = _
  var isActive: Boolean = _
  var hasPresentationPrefs: Boolean = _

  @Convert(converter = classOf[DateTimeConverter])
  var startDate: DateTime = _

  @Convert(converter = classOf[DateTimeConverter])
  var endDate: DateTime = _

  @Convert(converter = classOf[DateTimeConverter])
  var deadline: DateTime = null

  var logo: String = _
  var thumbnail: String = _

  var iOSApp: String = _
  @Column(length = 10000)
  var geo: String = _
  @Column(length = 100000)
  var schedule: String = _
  @Column(length = 10000)
  var info: String = _

  @Convert(converter = classOf[DateTimeConverter])
  var ctime: DateTime = _
  @Convert(converter = classOf[DateTimeConverter])
  var mtime: DateTime = _

  @OneToMany(mappedBy = "conference", cascade = Array(CascadeType.ALL), orphanRemoval = true)
  var groups: JSet[AbstractGroup] = new JTreeSet[AbstractGroup]()

  @OneToMany
  @JoinTable(name = "conference_owners")
  var owners: JSet[Account] = new JTreeSet[Account]()
  @OneToMany(mappedBy = "conference")
  var abstracts: JSet[Abstract] = new JTreeSet[Abstract]()
  @OneToMany(mappedBy = "conference", cascade = Array(CascadeType.ALL), orphanRemoval = true)
  var topics: JSet[Topic] = new JTreeSet[Topic]()


  def formatDuration : String = {
    if (startDate == null || endDate == null) {
      return ""
    }

    if (startDate.year.get == endDate.year.get) {
      if (startDate.monthOfYear.get == endDate.monthOfYear.get) {
        if (startDate.dayOfMonth.get == endDate.dayOfMonth.get) {
          val dateFormatter = DateTimeFormat.forPattern("d MMMM yyyy")
          startDate.toString(dateFormatter)
        } else {
          startDate.monthOfYear.getAsText + " " + startDate.dayOfMonth.get + " - " + endDate.dayOfMonth.get + ", " + endDate.year.get
        }
      } else {
        val fmtDayMonth = DateTimeFormat.forPattern("MMMM, d")
        startDate.toString(fmtDayMonth) + " - " + endDate.toString(fmtDayMonth) + ", " + endDate.year.get
      }
    } else {
      val dateFormatter = DateTimeFormat.forPattern("MMMM d, yyyy")
      startDate.toString(dateFormatter) + " - " + endDate.toString(dateFormatter)
    }
  }

}

object Conference extends Model {

  def apply(uuid: Option[String],
            name: Option[String],
            short: Option[String],
            group: Option[String],
            cite: Option[String],
            link: Option[String],
            description: Option[String],
            isOpen: Option[Boolean],
            isPublished: Option[Boolean],
            isActive: Option[Boolean],
            hasPresentationPrefs: Option[Boolean],
            startDate: Option[DateTime],
            endDate: Option[DateTime],
            deadline: Option[DateTime] = null,
            logo: Option[String] = null,
            thumbnail: Option[String] = null,
            iOSApp: Option[String] = null,
            groups: Seq[AbstractGroup] = Nil,
            owners: Seq[Account] = Nil,
            abstracts: Seq[Abstract] = Nil,
            topics: Seq[Topic] = Nil,
            geo: Option[String] = null,
            schedule: Option[String] = null,
            info: Option[String] = null): Conference = {

    val conference = new Conference()

    conference.uuid        = unwrapRef(uuid)
    conference.name        = unwrapRef(name)
    conference.short       = unwrapRef(short)
    conference.group       = unwrapRef(group)
    conference.cite        = unwrapRef(cite)
    conference.link        = unwrapRef(link)
    conference.description = unwrapRef(description)
    conference.isOpen      = isOpen.getOrElse(false)
    conference.isPublished = isPublished.getOrElse(false)
    conference.isActive    = isActive.getOrElse(false)
    conference.hasPresentationPrefs = hasPresentationPrefs.getOrElse(false)
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

    conference.geo         = unwrapRef(geo)
    conference.schedule    = unwrapRef(schedule)
    conference.info        = unwrapRef(info)

    conference
  }

}

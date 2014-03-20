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
import javax.persistence._

/**
 * Model for a conference topic
 */
@Entity
@Table(uniqueConstraints = Array(new UniqueConstraint(columnNames = Array("TOPIC", "CONFERENCE_UUID"))))
class Topic extends Model {

  @Basic(optional = false)
  var topic: String = _
  @ManyToOne(optional = false)
  var conference: Conference = _

  override def toString : String = topic
}

object Topic {

  def apply(topic: String, conference: Option[Conference]) {
    val t = new Topic()


    t.topic = topic
    t.conference = unwrapRef(conference)

    t
  }

}

// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import javax.persistence.{ManyToOne, Entity}
import models.Model._

/**
 * Model for abstract groups.
 */
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

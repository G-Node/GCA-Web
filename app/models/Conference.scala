// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import java.util.{List => JList}

/**
 * A model for that represents a conference.
 *
 * Comment: there could be allot more information about a conference, but
 * maybe we should keep it simple for now.
 */
class Conference extends Model {

  var name: String = _

  var abstracts: JList[Abstract] = _

}

object Conference {

  def apply() : Conference = new Conference()

  def apply(name: String, abstracts: JList[Abstract] = null) : Conference = {
    val conference = new Conference()

    conference.name = name
    conference.abstracts = abstracts

    conference
  }

}

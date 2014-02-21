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
import javax.persistence.{OneToOne, Entity}

/**
 * A model for figures.
 */
@Entity
class Figure extends Model {

  var name: String = _
  var caption: String = _

  @OneToOne
  var abstr: Abstract = _

}

object Figure {

  def apply(uuid: Option[String],
            name: Option[String],
            caption: Option[String],
            abstr: Option[Abstract] = None) : Figure = {

    val figure = new Figure()

    figure.uuid     = unwrapRef(uuid)
    figure.name     = unwrapRef(name)
    figure.caption  = unwrapRef(caption)

    figure.abstr    = unwrapRef(abstr)

    figure
  }

}

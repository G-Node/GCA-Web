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
import javax.persistence.{Column, ManyToOne, Entity}

/**
 * A model for figures.
 */
@Entity
class Figure extends PositionedModel {

  @Column(length=300)
  var caption: String = _

  @ManyToOne
  var abstr: Abstract = _

}

object Figure {

  def apply(uuid: Option[String],
            caption: Option[String],
            position: Option[Int] = None,
            abstr: Option[Abstract] = None) : Figure = {

    val figure = new Figure()

    figure.uuid     = unwrapRef(uuid)
    figure.caption  = unwrapRef(caption)
    figure.position = unwrapVal(position)

    figure.abstr    = unwrapRef(abstr)

    figure
  }

}

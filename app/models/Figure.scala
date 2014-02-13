// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

/**
 * A model for figures.
 */
class Figure extends Model {

  var name: String = _
  var caption: String = _

}

object Figure {

  def apply() : Figure = new Figure()

  def apply(name: String, caption: String) : Figure = {
    val figure = new Figure()

    figure.name = name
    figure.caption = caption

    figure
  }

}

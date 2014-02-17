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
 * Very simple model for referenced literature.
 */
class Reference extends Model {

  var authors: String = _
  var title: String = _
  var year: Int = _
  var doi: String = _

  var abstr: Abstract = _

}


object Reference {

  def apply() : Reference = new Reference()

  def apply(uuid: String, authors: String, title: String, year: Int,
            doi: String, abstr: Abstract) : Reference = {

    val ref = new Reference()

    ref.uuid = uuid
    ref.authors = authors
    ref.title = title
    ref.year = year
    ref.doi = doi

    ref.abstr = abstr

    ref
  }

}

// Copyright © 2019, German Neuroinformatics Node (G-Node)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import javax.persistence.{Entity, ManyToOne}
import models.Model.{unwrapRef}

/**
 * Model for conference texts
 */
@Entity
class ConfText extends Model {

  var ctType: String = _
  var text: String = _

  @ManyToOne
  var conference: Conference = _

}


object ConfText {

  def apply(uuid: Option[String],
            ctType: Option[String],
            text: Option[String],
            conf: Option[Conference] = None) : ConfText = {

    val confText = new ConfText()

    confText.uuid       = unwrapRef(uuid)
    confText.ctType     = unwrapRef(ctType)
    confText.text       = unwrapRef(text)

    confText.conference       = unwrapRef(conf)

    confText
  }

}



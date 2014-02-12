// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service.models

import java.util.UUID

/**
 * Trait that defines stuff that is common for all models.
 * This trait may be extended with some other properties like timestamps
 * etc. when needed.
 */
trait Model {

  /**
   * The primary key of the model.
   */
  var uuid: String = Model.makeUUID()

}

object Model {

  def makeUUID() : String = {
    UUID.randomUUID().toString
  }

}

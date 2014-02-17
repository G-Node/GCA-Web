// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package models

import java.util.UUID
import javax.persistence.{Id, MappedSuperclass}

/**
 * Trait that defines stuff that is common for all models.
 * This trait may be extended with some other properties like timestamps
 * etc. when needed.
 */
@MappedSuperclass
class Model {

  /**
   * The primary key of the model.
   */
  @Id
  var uuid: String = _

}

object Model {

  def makeUUID() : String = {
    UUID.randomUUID().toString
  }

}

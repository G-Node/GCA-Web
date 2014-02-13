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
  var year: Int = _
  var title: String = _
  var doi: String = _

}

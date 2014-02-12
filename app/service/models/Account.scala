// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service.models

import java.util.{List => JList}

/**
 * Model class for accounts.
 */
class Account extends Model {

  var mail: String = _
  var abstracts: JList[Abstract] = _

}

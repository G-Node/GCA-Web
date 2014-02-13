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
 * A model class for abstracts
 */
class Abstract extends Model{

  var title: String = _
  var topic: String = _
  var text:  String = _
  var doi:   String = _
  var conflictOfInterest: String = _
  var acknowledgements: String = _

  var approved: Boolean = false
  var published: Boolean = false

  var conference : Conference = _
  var figure: Figure = _
  var owners:  JList[Account] = _
  var authors: JList[Author] = _
  var affiliations: JList[Affiliation] = _
  var references: JList[Reference] = _

}

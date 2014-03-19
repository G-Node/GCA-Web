package models

import java.util.{Set => JSet}

/**
 * Interface that defines ownership.
 * Must be implemented in Models that support logic to add / remove owners.
 */
trait Owned {

  var uuid: String
  var owners: JSet[Account]

}

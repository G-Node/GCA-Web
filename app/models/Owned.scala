package models

import java.util.{Set => JSet}
import scala.collection.JavaConversions._

/**
 * Interface that defines ownership.
 * Must be implemented in Models that support logic to add / remove owners.
 */
trait Owned {

  var uuid: String
  var owners: JSet[Account]

  def isOwner(account: Account): Boolean = {
    val ownersList: Seq[Account] = asScalaSet(owners).toSeq
    ownersList.contains(account)
  }

  def canRead(account: Account): Boolean = {
    isOwner(account) || account.isAdmin
  }

  def canWrite(account: Account): Boolean = {
    isOwner(account)
  }
}

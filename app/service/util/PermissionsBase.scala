package service.util

import javax.persistence.EntityNotFoundException

import models.{Account, Owned}
import plugins.DBUtil._

import scala.collection.JavaConversions._


trait PermissionsBase  {

  /**
   * Set owners for an existing Owned-type object.
   * This is only permitted if the account owns the object.
   *
   * @param obj The Owned-type object to update.
   * @param account    The account who wants to update the object.
   * @param owners     The list of actual owners.
   */
  def setPermissions(obj: Owned, account: Account, owners: List[Account]) : List[Account] = {

    if (owners.isEmpty)
      throw new IllegalArgumentException("Owners list cannot be empty")

    transaction { (em, tx) =>

      val verified = for (user <- owners) yield {
        em.find(classOf[Account], user.uuid) match {
          case null => throw new EntityNotFoundException("Unable to find account with uuid = " + user.uuid)
          case a => a
        }
      }

      if (obj.uuid == null)
        throw new IllegalArgumentException("Unable to update an object without uuid")

      val objChecked = em.find(obj.getClass, obj.uuid)
      if (objChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + obj.uuid)

      if (!objChecked.canWrite(account))
        throw new IllegalAccessException("No permissions for object with uuid = " + obj.uuid)

      objChecked.owners.toList.foreach { owner =>
        if (! verified.contains(owner))
          objChecked.owners.remove(owner)
      }

      verified.foreach { owner =>
        if (! objChecked.owners.contains(owner))
          objChecked.owners.add(owner)
      }

      objChecked.owners.toList
    }
  }

  /**
   * Get permissions of an existing object.
   * This is only permitted if the account owns the object.
   *
   * @param obj        object with certain permissions.
   * @param account    The account who wants to access permissions.
   */
  def getPermissions(obj: Owned, account: Account) : List[Account] = {
    transaction { (em, tx) =>
      if (obj.uuid == null)
        throw new IllegalArgumentException("Unable to update an object without uuid")

      val objChecked = em.find(obj.getClass, obj.uuid)
      if (objChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + obj.uuid)

      if (!objChecked.canRead(account))
        throw new IllegalAccessException("No permissions for object with uuid = " + obj.uuid)

      objChecked.owners.toList
    }
  }
}

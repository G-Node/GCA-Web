package service.util

import javax.persistence.EntityNotFoundException
import models.{Owned, Account}
import collection.JavaConversions._

trait PermissionsBase extends DBUtil  {

  /**
   * Validates, that given:
   * - object has uuid and is real
   * - account is real
   * - account owns the object.
   *
   * Returns the refreshed object from the database.
   *
   * @param obj The Owned-type object to update.
   * @param account    The account who wants to update the object.
   *
   * @throws IllegalArgumentException If the object has no uuid
   * @throws EntityNotFoundException If the object or the user does not exist
   * @throws IllegalAccessException If account is not an owner.
   */
  def validate(obj: Owned, account: Account) = {

    dbTransaction { (em, tx) =>

      if (obj.uuid == null)
        throw new IllegalArgumentException("Unable to update an object without uuid")

      val accountChecked = em.find(classOf[Account], account.uuid) match {
        case null => throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)
        case a => a
      }

      val objChecked = em.find(obj.getClass, obj.uuid)
      if (objChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + obj.uuid)

      if (!objChecked.owners.toList.contains(accountChecked))
        throw new IllegalAccessException("No permissions for object with uuid = " + obj.uuid)

      objChecked
    }
  }

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

    val updated = dbTransaction { (em, tx) =>

      val verified = for (user <- owners) yield {
        em.find(classOf[Account], user.uuid) match {
          case null => throw new EntityNotFoundException("Unable to find account with uuid = " + user.uuid)
          case a => a
        }
      }

      val objChecked = validate(obj, account)

      objChecked.owners.toList.foreach { owner =>
        objChecked.owners.remove(owner)
      }

      verified.foreach { owner =>
        objChecked.owners.add(owner)
      }

      em.merge(objChecked)
    }

    updated.owners.toList
  }

  /**
   * Get permissions of an existing object.
   * This is only permitted if the account owns the object.
   *
   * @param obj        object with certain permissions.
   * @param account    The account who wants to access permissions.
   */
  def getPermissions(obj: Owned, account: Account) : List[Account] = {
    validate(obj, account).owners.toList
  }
}

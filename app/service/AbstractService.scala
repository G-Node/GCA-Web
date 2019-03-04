// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import java.io.File
import javax.persistence.{EntityNotFoundException, TypedQuery}

import play.Play
import models._
import plugins.DBUtil._
import service.util.PermissionsBase

import scala.collection.JavaConversions._
import scala.collection.mutable.{Map => MMap}

import org.joda.time.{DateTimeZone, DateTime}


//for the patch method
abstract class PatchOp
case class PatchAddSortId(id: Int) extends PatchOp
case class PatchAddDOI(doi: String) extends PatchOp

/**
 * Service class that provides data access logic for abstracts and nested
 * authors and affiliations.
 */
class AbstractService(figPath: String) extends PermissionsBase {

  /**
   * List all published abstracts that belong to a conference.
   *
   * @param conference The conference for which to list the abstracts.
    * @return All published abstracts that are associated with a
   *         certain conference.
   */
  def list(conference: Conference) : Seq[Abstract] = {

    if(!conference.isPublished) {
      return Seq.empty[Abstract]
    }

    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.conference c
           WHERE c.uuid = :uuid AND a.state = :state
           ORDER BY a.sortId, a.title"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", conference.uuid)
      query.setParameter("state", AbstractState.Accepted)
      asScalaBuffer(query.getResultList)
    }
  }

  /**
  * List all abstracts (independent of the state) that belong to a conference.
  *
  * @param conference The conference for which to list the abstracts.
    * @return All published abstracts that are associated with a
  *         certain conference.
    */
  def listAll(conference: Conference) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.conference c
           WHERE c.uuid = :uuid
           ORDER BY a.sortId, a.title"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", conference.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  /**
   * List all published and unpublished abstracts that belong to an account.
   *
   * @param account The account for which to list the abstracts.
    * @return All abstracts that belong to an account.
   */
  def listOwn(account: Account) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners o
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE o.uuid = :uuid
          ORDER BY a.sortId, a.title"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", account.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  def listOwn(conference: Conference, account: Account) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners o
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference c
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE c.uuid = :ConfUuid AND o.uuid = :OwnerUuid
           ORDER BY a.sortId, a.title"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("ConfUuid", conference.uuid)
      query.setParameter("OwnerUuid", account.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  /**
    * List all published and unpublished favourite abstracts that belong to an account.
    *
    * @param account The account for which to list the abstracts.
    * @return All abstracts that are favourites of the current account.
    */
  def listFavourite(account: Account) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners
           LEFT JOIN FETCH a.favUsers f
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE f.uuid = :uuid
          ORDER BY a.sortId, a.title"""
      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", account.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  def listFavourite(conference: Conference, account: Account) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners
           LEFT JOIN FETCH a.favUsers f
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference c
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE c.uuid = :ConfUuid AND f.uuid = :FavUserUuid
           ORDER BY a.sortId, a.title"""
      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("ConfUuid", conference.uuid)
      query.setParameter("FavUserUuid", account.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  def listIsFavourite(conference: Conference, account: Account) : Seq[Abstract] = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.favUsers f
           LEFT JOIN FETCH a.conference c
           WHERE c.uuid = :ConfUuid AND f.uuid = :FavUserUuid
           ORDER BY a.sortId, a.title"""
      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("ConfUuid", conference.uuid)
      query.setParameter("FavUserUuid", account.uuid)
      asScalaBuffer(query.getResultList)
    }
  }

  /**
   * Return a published (= Accepted && Conference.isPublished) abstract by id.
   *
   * @param id The id of the abstract.
    * @return The abstract with the specified id.
    * @throws NoResultException If the conference was not found
   */
  def get(id: String) : Abstract= {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference c
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE c.isPublished = TRUE AND a.state = :state AND a.uuid = :uuid"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", id)
      query.setParameter("state", AbstractState.Accepted)
      query.getSingleResult
    }
  }

  /**
   * Return an abstract with a certain id, that is accessible for an account.
   * The abstract doesnt need to be published if the account has appropriate
   * access.
   *
   * @param id      The id of the abstract.
   * @param account The account who wants to request the abstract.
    * @return The abstract with the specified id.
    * @throws EntityNotFoundException If the account does not exist
   * @throws IllegalAccessException if not accessible
   * @throws NoResultException If was not found
   */
  def getOwn(id: String, account: Account) : Abstract = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners o
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference c
           LEFT JOIN       c.owners co
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE a.uuid = :uuid"""

      // this apparently useless query is needed to avoid strange caching behaviour related to #285
      val queryUserStr =  """SELECT acc FROM Account acc LEFT JOIN acc.abstracts a WHERE a.uuid = :uuid"""
      em.createQuery(queryUserStr, classOf[Account])
        .setParameter("uuid", id)
        .getResultList

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", id)
      val abstr = query.getSingleResult

      if (!(abstr.isOwner(account) || abstr.conference.isOwner(account) || account.isAdmin))
        throw new IllegalAccessException("No permissions for abstract with uuid = " + abstr.uuid)

      abstr
    }
  }

  /**
    * Return an abstract with a certain id, that is accessible for an account.
    * The abstract doesnt need to be published if the account has appropriate
    * access.
    *
    * @param id      The id of the abstract.
    * @param account The account who wants to request the abstract.
    * @return The abstract with the specified id.
    * @throws EntityNotFoundException If the account does not exist
    * @throws IllegalAccessException if not accessible
    * @throws NoResultException If was not found
    */
  def getFav(id: String, account: Account) : Abstract = {
    query { em =>
      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners
           LEFT JOIN FETCH a.favUsers
           LEFT JOIN FETCH a.authors
           LEFT JOIN FETCH a.affiliations
           LEFT JOIN FETCH a.conference
           LEFT JOIN FETCH a.figures
           LEFT JOIN FETCH a.references
           WHERE a.uuid = :uuid"""
      // this apparently useless query is needed to avoid strange caching behaviour related to #285
      val queryUserStr =  """SELECT acc FROM Account acc LEFT JOIN acc.favAbstracts a WHERE a.uuid = :uuid"""
      em.createQuery(queryUserStr, classOf[Account])
        .setParameter("uuid", id)
        .getResultList
      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)
      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", id)
      val abstr = query.getSingleResult
      abstr
    }
  }
  /**
    * Get permissions of an existing object.
    * This is only permitted if the account owns the object.
    *
    * @param obj        object with certain permissions.
    * @param account    The account who wants to access permissions.
    */
  def getFavouriteUsers(obj: Abstract, account: Account) : List[Account] = {
    transaction { (em, tx) =>
      if (obj.uuid == null)
        throw new IllegalArgumentException("Unable to update an object without uuid")
      val objChecked = em.find(obj.getClass, obj.uuid)
      if (objChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + obj.uuid)
      objChecked.favUsers.toList
    }
  }

  /**
   * Create a new abstract.
   * This is only permitted if the account is one of the owners.
   *
    * @param abstr   The Abstract to create.
   * @param conference  The the id of the conference.
   * @param account The account who wants to perform the creation.
    * @return The created and persisted abstract.
   */
  def create(abstr : Abstract, conference: Conference, account: Account) : Abstract = {
    val abstrCreated = transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val conferenceChecked = em.find(classOf[Conference], conference.uuid)
      if (conferenceChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + conference.uuid)

      if (abstr.uuid != null)
        throw new IllegalArgumentException("Unable to create an abstract with not null uuid")

      abstr.conference = conferenceChecked
      abstr.owners.add(accountChecked)

      arrangeAffiliations(abstr)

      abstr.references.foreach { reference =>
        reference.abstr = abstr
      }

      abstr.stateLog.add(StateLogEntry(abstr, AbstractState.InPreparation,
        account, Some("Initial abstract creation")))

      abstr.ctime = new DateTime(DateTimeZone.UTC)

      em.merge(abstr)
    }

    getOwn(abstrCreated.uuid, account)
  }

  /**
   * Update an existing abstract.
   * This is only permitted if the account is one of the owners.
   *
   * @param abstr   The Abstract to update.
   * @param account The account who wants to perform the update.
    * @return The updated and persisted abstract.
   */
  def update(abstr : Abstract, account: Account) : Abstract = {
    val abstrUpdated = transaction { (em, tx) =>

      if (abstr.uuid == null)
        throw new IllegalArgumentException("Unable to update an abstract with null uuid")

      val abstrChecked = em.find(classOf[Abstract], abstr.uuid)
      if (abstrChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + abstr.uuid)

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val isOwner = abstrChecked.isOwner(accountChecked)
      val isConfOwner = abstrChecked.conference.isOwner(accountChecked)
      val isAdmin = accountChecked.isAdmin
      if (! (isOwner || isConfOwner || isAdmin))
        throw new IllegalAccessException("No permissions for abstract with uuid = " + abstr.uuid)

      abstr.stateLog = abstrChecked.stateLog
      if(abstr.state != abstrChecked.state) {
        throw new IllegalAccessException(s"Trying to change state via update [uuid = $abstr.uuid]")
      }

      arrangeAffiliations(abstr)

      abstr.references.foreach { reference =>
        reference.abstr = abstr
      }

      abstr.owners = abstrChecked.owners
      abstr.conference = abstrChecked.conference
      abstr.figures = abstrChecked.figures
      abstr.ctime = abstrChecked.ctime

      val merged = em.merge(abstr)

      abstrChecked.authors.foreach { author =>
        if (!abstr.authors.contains(author))
          em.remove(author)
      }

      abstrChecked.affiliations.foreach { affiliation =>
        if (!abstr.affiliations.contains(affiliation))
          em.remove(affiliation)
      }

      abstrChecked.references.foreach { reference =>
        if (!abstr.references.contains(reference))
          em.remove(reference)
      }

      merged
    }

    getOwn(abstrUpdated.uuid, account)
  }


  /**
    * Add a favourite user from an abstract.
    *
    * @param abstr   The Abstract to update.
    * @param account The account who wants to perform the update.
    * @return The updated and persisted abstract.
    */
  def addFavUser(abstr : Abstract, account: Account) : Abstract = {
    val abstrUpdated = transaction { (em, tx) =>
      if (abstr.uuid == null)
        throw new IllegalArgumentException("Unable to update an abstract with null uuid")

      val abstrChecked = em.find(classOf[Abstract], abstr.uuid)
      if (abstrChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + abstr.uuid)

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      abstr.favUsers.add(account)
      val merged = em.merge(abstr)
      merged
    }
    get(abstrUpdated.uuid)
  }

  /**
    * Remove a favourite user from an abstract.
    *
    * @param abstr   The Abstract to update.
    * @param account The account who wants to perform the update.
    * @return The updated and persisted abstract.
    */
  def removeFavUser(abstr : Abstract, account: Account) : Abstract = {
    val abstrUpdated = transaction { (em, tx) =>

      if (abstr.uuid == null)
        throw new IllegalArgumentException("Unable to update an abstract with null uuid")

      val abstrChecked = em.find(classOf[Abstract], abstr.uuid)
      if (abstrChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + abstr.uuid)

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      abstr.favUsers.remove(account)
      val merged = em.merge(abstr)
      merged
    }
    get(abstrUpdated.uuid)
  }

  /**
   * Delete an abstract.
   * This is only permitted if the account is one of the owners.
   *
   * @param id      The id of the abstract to delete.
   * @param account The account who wants to perform the delete.
    * @throws IllegalArgumentException If the conference has no uuid
   * @throws EntityNotFoundException If the conference or the user does not exist
   * @throws IllegalAccessException If account is not an owner.
   */
  def delete(id: String, account: Account) : Unit = {
    transaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val abstrChecked = em.find(classOf[Abstract], id)
      if (abstrChecked == null)
        throw new EntityNotFoundException("Unable to find abstract with uuid = " + id)

      val isOwner = abstrChecked.isOwner(accountChecked)
      val isConfOwner = abstrChecked.conference.isOwner(accountChecked)
      val isAdmin = accountChecked.isAdmin
      if (! (isOwner || isConfOwner || isAdmin))
        throw new IllegalAccessException("No permissions for abstract with uuid = " + id)

      abstrChecked.figures.foreach( fig => {
        if (fig != null) {
          val file = new File(figPath, fig.uuid)
          if (file.exists())
            file.delete()
        }
      })

      abstrChecked.figures.foreach(em.remove(_))
      abstrChecked.authors.foreach(em.remove(_))
      abstrChecked.affiliations.foreach(em.remove(_))
      abstrChecked.references.foreach(em.remove(_))

      em.remove(abstrChecked)
    }
  }

  /**
   * List all state logs of a given abstract
    *
    * @param id       abstract id
   * @param account  account (for permission)
   * @return         Sorted sequence of log entries (newest first)
   */
  def listStates(id: String, account: Account) : Seq[StateLogEntry] = {
    transaction { (em, tx) =>

      val queryStr =
        """SELECT DISTINCT a FROM Abstract a
           LEFT JOIN FETCH a.owners o
           LEFT JOIN FETCH a.stateLog
           WHERE a.uuid = :uuid"""

      val query: TypedQuery[Abstract] = em.createQuery(queryStr, classOf[Abstract])
      query.setParameter("uuid", id)
      val abstr = query.getSingleResult

      if (!(account.isAdmin || abstr.isOwner(account) || abstr.conference.isOwner(account))) {
        throw new IllegalAccessException("No permissions for abstract with uuid = " + id +
          "for account with uuid = " + account.uuid)
      }

      abstr.stateLog.toSeq.sortWith (_.timestamp.getMillis > _.timestamp.getMillis)
    }
  }

  def setState(abstr: Abstract, state: AbstractState.State, editor: Account, message: Option[String]) = {
    transaction { (em, tx) =>

      val abstrMerged = em.merge(abstr)
      abstrMerged.state = state
      abstrMerged.touch()

      val log = em.merge(StateLogEntry(abstrMerged, state, editor, message))
      abstrMerged.stateLog.add(log)

      abstrMerged.stateLog.toSeq.sortWith (_.timestamp.getMillis > _.timestamp.getMillis)
    }
  }

  def patch(abstr: Abstract, patches: List[PatchOp]) = {
    transaction { (em, tx) =>

      patches.foreach {
        case PatchAddSortId(id: Int) => abstr.sortId = id
        case PatchAddDOI(doi: String) => abstr.doi = doi
        case _ => throw new IllegalArgumentException("Invalid value to patch")
      }

      abstr.touch()
      em.merge(abstr)
    }
  }

  private def arrangeAffiliations(abstr: Abstract) = {
    val transformation = MMap[Int, Int]()
    var i = 0

    // calculate affiliation positions according to the authors positions
    val sorted = asScalaSet(abstr.authors).toList.sortBy(_.position)
    sorted.foreach(author => {
      for (pos <- author.affiliationPositions) {
        if (!transformation.keySet.contains(pos)) {
          transformation.put(pos, i)
          i += 1
        }
      }
    })

    // re-position affiliations
    abstr.affiliations.foreach { affiliation =>
      affiliation.abstr = abstr

      transformation.get(affiliation.position) match {
        case Some(pos) => affiliation.position = pos
        case None =>
      }
    }

    abstr.authors.foreach { author =>
      author.abstr = abstr

      for (pos <- author.affiliationPositions) {
        abstr.affiliations.find(_.position == transformation.get(pos).get) match {
          case Some(affiliation) =>
            affiliation.authors.add(author)
            author.affiliations.add(affiliation)
          case _ =>
        }
      }
    }
  }

}


object AbstractService {

  def apply[A]() = {
    new AbstractService(Play.application().configuration().getString("file.fig_path", "./figures"))
  }

  def apply(figPath: String) = {
    new AbstractService(figPath)
  }

}

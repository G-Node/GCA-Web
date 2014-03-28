// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import collection.JavaConversions._

import play.api._
import models._
import javax.persistence._
import service.util.{PermissionsBase, DBUtil}
import java.net.URLDecoder

/**
 * Service class for that implements data access logic for conferences.
 *
 * TODO prefetch stuff
 * TODO write test
 */
class ConferenceService(val emf: EntityManagerFactory) extends PermissionsBase {

  /**
   * List all available conferences.
   *
   * @return All conferences.
   */
  def list() : Seq[Conference] = {
    dbQuery { em =>
      val queryStr =
        """SELECT DISTINCT c FROM Conference c
           LEFT JOIN FETCH c.groups
           LEFT JOIN FETCH c.owners
           LEFT JOIN FETCH c.abstracts
           LEFT JOIN FETCH c.topics
           ORDER BY c.startDate DESC
        """


      val query : TypedQuery[Conference] = em.createQuery(queryStr, classOf[Conference])
      asScalaBuffer(query.getResultList)
    }
  }

  /**
   * List all conferences that belong to a certain account.
   *
   * @param account The account to list the conferences for.
   *
   * @return All conferences that belong tho the account.
   */
  def listOwn(account: Account) : Seq[Conference] = {
    dbQuery { em =>
      val queryStr =
        """SELECT DISTINCT c FROM Conference c
           LEFT JOIN FETCH c.groups
           INNER JOIN FETCH c.owners o
           LEFT JOIN FETCH c.abstracts
           LEFT JOIN FETCH c.topics
           WHERE o.uuid = :uuid
           ORDER BY c.startDate DESC"""

      val query : TypedQuery[Conference] = em.createQuery(queryStr, classOf[Conference])
      query.setParameter("uuid", account.uuid)

      asScalaBuffer(query.getResultList)
    }
  }

  def listWithAbstractsOfAccount(account: Account) : Seq[Conference] = {
    dbQuery { em =>
      val queryStr =
        """SELECT DISTINCT c FROM Conference c
           INNER JOIN FETCH c.owners o
           INNER JOIN FETCH c.abstracts a
           INNER JOIN a.owners ao
           LEFT JOIN FETCH c.topics
           WHERE ao.uuid = :uuid
           ORDER BY c.startDate DESC"""

      val query : TypedQuery[Conference] = em.createQuery(queryStr, classOf[Conference])
      query.setParameter("uuid", account.uuid)

      asScalaBuffer(query.getResultList)
    }
  }

  /**
   * Get a conference specified by its id.
   *
   * @param id The id of the conference.
   *
   * @return The specified conference.
   *
   * @throws NoResultException If the conference was not found
   */
  def get(id: String) : Conference = {
    dbQuery { em =>
      val queryStr =
        """SELECT DISTINCT c FROM Conference c
           LEFT JOIN FETCH c.groups
           LEFT JOIN FETCH c.owners
           LEFT JOIN FETCH c.abstracts
           LEFT JOIN FETCH c.topics
           WHERE c.uuid = :uuid or c.short = :short"""

      val query : TypedQuery[Conference] = em.createQuery(queryStr, classOf[Conference])
      query.setParameter("uuid", id)
      query.setParameter("short", URLDecoder.decode(id, "UTF-8"))

      query.getSingleResult
    }
  }

  /**
   * Create a new conference from the data of the given conference
   * object.
   *
   * @param conference The conference object that contains the data of the new
   *                   conference.
   * @param account    The account that owns the conference.
   *
   * @return The created conference.
   *
   * @throws EntityNotFoundException If the account does not exist.
   * @throws IllegalArgumentException If the conference has an uuid.
   */
  def create(conference: Conference, account: Account) : Conference = {

    val conf = dbTransaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      if (conference.uuid != null)
        throw new IllegalArgumentException("Unable to create conference with not null uuid")

      if (!accountChecked.isAdmin)
        throw new IllegalAccessException("No permissions for creating a conference")

      conference.owners.add(account)

      conference.groups.foreach { group =>
        Logger.debug("Adding group:" + group.toString)
        group.conference = conference
      }

      conference.topics.foreach { topic =>
        topic.conference = conference
      }

      em.merge(conference)
    }

    get(conf.uuid)
  }

  /**
   * Update an existing conference.
   * This is only permitted if the account is one of the owners of the conference.
   *
   * @param conference The conference to update.
   * @param account    The account who wants to update the conference.

   * @return The updated conference.
   *
   * @throws IllegalArgumentException If the conference has no uuid
   * @throws EntityNotFoundException If the conference or the user does not exist
   * @throws IllegalAccessException If account is not an owner.
   */
  def update(conference: Conference, account: Account) : Conference = {
    val conf = dbTransaction { (em, tx) =>

      if (conference.uuid == null)
        throw new IllegalArgumentException("Unable to update a conference without uuid")

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val confChecked = em.find(classOf[Conference], conference.uuid)
      if (confChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + conference.uuid)

      if (! (confChecked.owners.contains(accountChecked) || accountChecked.isAdmin))
        throw new IllegalAccessException("No permissions for conference with uuid = " + conference.uuid)

      conference.owners = confChecked.owners

      conference.groups.foreach { group =>
        group.conference = conference
      }

      conference.topics.foreach { topic =>
        confChecked.topics.find {
          _.topic == topic.topic
        }.map {
          t => topic.uuid = t.uuid
        }

        topic.conference = conference
      }

      val merged = em.merge(conference)

      confChecked.groups.foreach { group =>
        if (!merged.groups.contains(group)) {
          em.remove(group)
        }
      }

      confChecked.topics.foreach { topic =>
        if (!merged.topics.contains(topic)) {
          em.remove(topic)
        }
      }

      merged
    }

    get(conf.uuid)
  }

  /**
   * Delete an existing conference.
   * This is only permitted if the account owns the conference.
   *
   * @param id      The id of the conference to delete.
   * @param account The account who wants to perform the delete.
   *
   * @throws IllegalArgumentException If the conference has no uuid
   * @throws EntityNotFoundException If the conference or the user does not exist
   * @throws IllegalAccessException If account is not an owner.
   */
  def delete(id: String, account: Account) : Unit = {
    dbTransaction { (em, tx) =>

      val accountChecked = em.find(classOf[Account], account.uuid)
      if (accountChecked == null)
        throw new EntityNotFoundException("Unable to find account with uuid = " + account.uuid)

      val confChecked = em.find(classOf[Conference], id)
      if (confChecked == null)
        throw new EntityNotFoundException("Unable to find conference with uuid = " + id)

      if (! (confChecked.owners.contains(accountChecked) || accountChecked.isAdmin))
        throw new IllegalAccessException("No permissions for conference with uuid = " + id)

      confChecked.groups.foreach(em.remove(_))
      confChecked.topics.foreach(em.remove(_))

      em.remove(confChecked)
    }
  }
}


object ConferenceService {

  def apply() : ConferenceService = {
    new ConferenceService(
      Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    )
  }

}

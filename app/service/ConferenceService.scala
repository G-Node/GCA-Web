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

import models._
import javax.persistence._
import play.db.jpa.Transactional

/**
 * Service class for that implements data access logic for conferences.
 *
 * TODO prefetch stuff
 * TODO write test
 */
class ConferenceService(emf: EntityManagerFactory) {

  /**
   * List all available conferences.
   *
   * @return All conferences.
   */
  def list() : Seq[Conference] = {
    transactional { (em, tx) =>
      val queryStr = "SELECT c FROM Conference c"

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
    transactional { (em, tx) =>
      val queryStr = "SELECT c FROM Conference c INNER JOIN FETCH c.owners o WHERE o.uuid = :uuid"

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
   */
  def get(id: String) : Conference = {
    throw new NotImplementedError()
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
   */
  def create(conference: Conference, account: Account) : Conference = {
    throw new NotImplementedError()
  }

  /**
   * Update an existing conference.
   * This is only permitted if the account is one of the owners of the conference.
   *
   * @param conference The conference to update.
   * @param account    The account who wants to update the conference.

   * @return The updated conference.
   */
  def update(conference: Conference, account: Account) : Conference = {
    throw new NotImplementedError()
  }

  /**
   * Delete an existing conference.
   * This is only permitted if the account owns the conference.
   *
   * @param id      The id of the conference to delete.
   * @param account The account who wants to perform the delete.
   *
   * @return True if the conference was deleted, false otherwise.
   */
  def delete(id: String, account: Account) : Boolean = {
    throw new NotImplementedError()
  }

  def transactional[T](f : (EntityManager, EntityTransaction) => T) : T = {

    synchronized {

      var em : EntityManager = null
      var tx : EntityTransaction = null

      try {

        em = emf.createEntityManager()
        tx = em.getTransaction
        tx.begin()

        f(em, tx)

      } catch {

        case ex : Exception =>
          if (tx != null && tx.isActive) tx.rollback()
          throw ex

      } finally {

        if (tx != null && tx.isActive) tx.commit()
        if (em != null && em.isOpen) em.close()

      }

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

// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import service.models._

/**
 * Service class that provides data access logic for abstracts and nested
 * authors and affiliations.
 */
class AbstractService {

  /**
   * List all published abstracts that belong to a conference.
   *
   * @param conference The conference for which to list the abstracts.
   *
   * @return All published abstracts that are associated with a
   *         certain conference.
   */
  def list(conference: Conference) : Seq[Abstract] = {
    throw new NotImplementedError()
  }

  /**
   * List all published and unpublished abstracts that belong to an account.
   *
   * @param account The account for which to list the abstracts.
   *
   * @return All abstracts that belong to an account.
   */
  def listOwn(account: Account) : Seq[Abstract] = {
    throw new NotImplementedError()
  }

  /**
   * Return a published abstract by id.
   *
   * @param id The id of the abstract.
   *
   * @return The published abstract of None if the abstract does not exist
   *         or is not published.
   */
  def get(id: String) : Option[Abstract] = {
    throw new NotImplementedError()
  }

  /**
   * Return an abstract with a certain id, that is owned by an account.
   * The abstract doesnt need to be published if the account is an owner
   * of the abstract.
   *
   * @param id      The id of the abstract.
   * @param account The account who wants to get the abstract.
   *
   * @return The published abstract of None if the abstract does not exist
   *         or is not published.
   */
  def getOwn(id: String, account: Account) : Option[Abstract] = {
    throw new NotImplementedError()
  }

  /**
   * Create a new abstract.
   * This is only permitted if the account is one of the owners.
   *
   *
   * @param abstr   The Abstract to create.
   * @param account The account who wants to perform the creation.
   *
   * @return The created and persisted abstract.
   */
  def create(abstr : Abstract, account: Account) : Abstract = {
    throw new NotImplementedError()
  }

  /**
   * Update an existing abstract.
   * This is only permitted if the account is one of the owners.
   *
   * @param abstr   The Abstract to update.
   * @param account The account who wants to perform the update.
   *
   * @return The updated and persisted abstract.
   */
  def update(abstr : Abstract, account: Account) : Abstract = {
    throw new NotImplementedError()
  }

  /**
   * Delete an abstract.
   * This is only permitted if the account is one of the owners.
   *
   * @param id      The id of the abstract to delete.
   * @param account The account who wants to perform the delete.
   *
   * @return True if the abstract was deleted.
   */
  def delete(id: String, account: Account) : Boolean = {
    throw new NotImplementedError()
  }

}

// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import org.scalatest.junit.JUnitSuite
import org.junit._
import play.api.test.FakeApplication
import service.ConferenceService
import play.api.Play
import javax.persistence.{EntityNotFoundException, NoResultException, EntityManagerFactory, Persistence}
import models.{Conference, Account}
import service.util.DBUtil

/**
 * Test
 */
class ConferenceServiceTest extends JUnitSuite with DBUtil {

  var service : ConferenceService = _
  var account : Account = _
  var evilAccount : Account = _
  var conference : Conference = _
  var emf : EntityManagerFactory = _

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    dbTransaction { (em, tx) =>
      conference = em.merge(Conference(None, Some("conf1")))
      em.merge(Conference(None, Some("conf2")))

      account = em.merge(Account(None, Some("mail")))
      evilAccount = em.merge(Account(None, Some("mail@eviltwin.ork")))

      conference.owners.add(account)
      conference = em.merge(conference)
    }
    service = new ConferenceService(emf)
  }

  @After
  def after() : Unit = {
    dbTransaction { (em, tx) =>
      em.createQuery("DELETE FROM Conference").executeUpdate()
      em.createQuery("DELETE FROM Account").executeUpdate()
    }
  }

  @Test
  def testList() : Unit = {
    val list = service.list()
    assert(list.size == 2)
  }

  @Test
  def testListOwn() : Unit = {
    var list = service.listOwn(account)

    assert(list.size == 1)
    assert(list.head.name == conference.name)

    list = service.listOwn(Account(Some("uuid"), Some("foo@bar.com")))

    assert(list.size == 0)
  }

  @Test
  def testGet() : Unit = {
    val c = service.get(conference.uuid)

    assert(c.uuid == conference.uuid)

    intercept[NoResultException] {
      service.get("uuid")
    }
  }

  @Test
  def testCreate() : Unit = {
    dbTransaction { (em, tx) =>
      em.createQuery("DELETE FROM Conference").executeUpdate()
    }
    val c = service.create(Conference(None, Some("fooconf")), account)

    assert(c.uuid != null)
    assert(c.name == "fooconf")
    assert(c.owners.iterator.next.uuid == account.uuid)

    intercept[IllegalArgumentException] {
      service.create(Conference(Some("uuid"), Some("wrongconf")), account)
    }

    intercept[EntityNotFoundException] {
      service.create(Conference(None, Some("fooconf two")), Account(Some("uuid"), Some("foo@bar.com")))
    }
  }

  @Test
  def testUpdate() : Unit = {
    conference.name = "changed conference name"
    val c = service.update(conference, account)

    assert(c.name == "changed conference name")

    intercept[IllegalArgumentException] {
      service.update(Conference(None, Some("wrongconf")), account)
    }

    intercept[EntityNotFoundException] {
      service.update(Conference(Some("uuid"), Some("wrongconf")), account)
    }

    intercept[EntityNotFoundException] {
      service.update(conference, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      service.update(conference, evilAccount)
    }
  }

  @Test
  def testDelete() : Unit = {
    intercept[EntityNotFoundException] {
      service.delete("uuid", account)
    }

    intercept[EntityNotFoundException] {
      service.delete(conference.uuid, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      service.delete(conference.uuid, evilAccount)
    }

    service.delete(conference.uuid, account)

    intercept[EntityNotFoundException] {
      service.delete(conference.uuid, account)
    }
  }

}

object ConferenceServiceTest {

  var app: FakeApplication = null

  @BeforeClass
  def beforeClass() = {
    app = new FakeApplication()
    Play.start(app)
  }

  @AfterClass
  def afterClass() = {
    Play.stop()
  }

}

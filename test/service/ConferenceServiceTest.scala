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
import collection.JavaConversions._
import play.api.test.FakeApplication
import play.api.Play
import javax.persistence._
import models._
import service.util.DBUtil

/**
 * Test
 */
class ConferenceServiceTest extends JUnitSuite with DBUtil {

  var emf : EntityManagerFactory = _
  var srv : ConferenceService = _
  var assets : Assets = _

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
    srv = new ConferenceService(emf)
  }

  @Test
  def testList() : Unit = {
    val list = srv.list()
    assert(list.size == 3)
  }

  @Test
  def testListOwn() : Unit = {
    var list = srv.listOwn(assets.alice)
    assert(list.size == 3)

    list = srv.listOwn(assets.eve)
    assert(list.size == 0)
  }

  @Test
  def testGet() : Unit = {
    val c = srv.get(assets.conferences(0).uuid)

    assert(c.uuid == assets.conferences(0).uuid)

    intercept[NoResultException] {
      srv.get("uuid")
    }
  }

  @Test
  def testCreate() : Unit = {
    val c = srv.create(Conference(None, Some("fooconf")), assets.alice)

    assert(c.uuid != null)
    assert(c.name == "fooconf")
    assert(c.owners.head.uuid == assets.alice.uuid)

    intercept[IllegalArgumentException] {
      srv.create(Conference(Some("uuid"), Some("wrongconf")), assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.create(Conference(None, Some("fooconf two")), Account(Some("uuid"), Some("foo@bar.com")))
    }
  }

  @Test
  def testUpdate() : Unit = {
    val conference = assets.conferences(1)
    conference.name = "changed conference name"
    val c = srv.update(conference, assets.alice)

    assert(c.name == "changed conference name")

    intercept[IllegalArgumentException] {
      srv.update(Conference(None, Some("wrongconf")), assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.update(Conference(Some("uuid"), Some("wrongconf")), assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.update(conference, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      srv.update(conference, assets.eve)
    }
  }

  @Test
  def testDelete() : Unit = {
    val conference = assets.conferences(2)
    intercept[EntityNotFoundException] {
      srv.delete("uuid", assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.delete(conference.uuid, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      srv.delete(conference.uuid, assets.eve)
    }

    srv.delete(conference.uuid, assets.alice)

    intercept[EntityNotFoundException] {
      srv.delete(conference.uuid, assets.alice)
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

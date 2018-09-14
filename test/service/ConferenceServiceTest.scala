// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import javax.persistence._

import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.test.FakeApplication
import models._

import scala.collection.JavaConversions._

/**
 * Test
 */
class ConferenceServiceTest extends JUnitSuite {

  var srv : ConferenceService = _
  var assets : Assets = _

  @Before
  def before() : Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    srv = ConferenceService()
  }

  @Test
  def testList() : Unit = {
    val list = srv.list()
    assert(list.size == 3)
  }

  @Test
  def testListWithGroup() : Unit = {
    val list = srv.listWithGroup("BCCN")
    assert(list.size == 2)
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
    val c = srv.create(Conference(None, Some("fooconf"), Some("F1"), Some("G"), Some("F"),
                       None, None, Some(false), Some(true), Some(false), Some(true), None, None),
                       assets.alice)

    assert(c.uuid != null)
    assert(c.ctime != null)
    assert(c.mtime != null)
    assert(c.name == "fooconf")
    assert(c.owners.head.uuid == assets.alice.uuid)

    intercept[IllegalArgumentException] {
      srv.create(Conference(Some("uuid"), Some("wrongconf"), Some("bla"), Some("G"),
                 None, None, None, Some(false), Some(true), Some(false), Some(true), None, None),
                 assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.create(Conference(None, Some("fooconf two"), Some("XX"), Some("G"), Some("X"),
                 None, None, Some(false), Some(true), Some(false), Some(true), None, None, None),
        Account(Some("uuid"), Some("foo@bar.com")))
    }
  }

  @Test
  def testUpdate() : Unit = {
    val conference = assets.conferences(1)
    conference.name = "changed conference name"
    val ctime = conference.ctime
    conference.ctime = null
    val c = srv.update(conference, assets.alice)

    assert(c.name == "changed conference name")
    assert(c.ctime == ctime)

    intercept[IllegalArgumentException] {
      srv.update(Conference(None, Some("wrongconf"), Some("XX"), Some("G"), Some("X"),
                 None, None, Some(false), Some(true), Some(false), Some(true), None, None, None), assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.update(Conference(Some("uuid"), Some("wrongconf"), Some("XX"), Some("G"), Some("X"),
                 None, None, Some(false), Some(true), Some(false), Some(true), None, None, None), assets.alice)
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

  @Test
  def testPermissions() : Unit = {
    val conference = assets.conferences(0) // alice is the only owner
    val alice = assets.alice // or conference.owners.toList(0)
    val bob = assets.bob
    val eve = assets.eve

    assert(srv.getPermissions(conference, alice).contains(alice))

    assert(srv.setPermissions(conference, alice, List[Account](bob, eve)).contains(eve))

    assert(!srv.getPermissions(conference, bob).contains(alice))

    intercept[IllegalArgumentException] {
      srv.setPermissions(conference, bob, List[Account]())
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

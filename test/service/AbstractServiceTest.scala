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
import play.api.Play
import javax.persistence.{EntityNotFoundException, NoResultException, EntityManagerFactory, Persistence}
import service.util.DBUtil
import models.Account

/**
 * Test for the abstracts service layer
 */
class AbstractServiceTest extends JUnitSuite with DBUtil {

  var emf : EntityManagerFactory = _
  var srv : AbstractService = _
  var assets: Assets = _

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.fillDB()
    srv = new AbstractService(emf)
  }

  @After
  def after() : Unit = {
    assets.killDB()
  }

  @Test
  def testList() : Unit = {
    var abstracts = srv.list(assets.conferences(0))
    assert(abstracts.size == assets.abstracts.size)

    abstracts = srv.list(assets.conferences(1))
    assert(abstracts.size == 0)
  }

  @Test
  def testListOwn() : Unit = {
    var abstracts = srv.listOwn(assets.alice)
    assert(abstracts.size == assets.abstracts.size)

    abstracts = srv.listOwn(assets.bob)
    assert(abstracts.size == assets.abstracts.size)

    abstracts = srv.listOwn(assets.eve)
    assert(abstracts.size == 0)
  }

  @Test
  def testGet() : Unit = {
    val abstr = srv.get(assets.abstracts(0).uuid)
    assert(abstr.uuid == assets.abstracts(0).uuid)

    intercept[NoResultException] {
      srv.get(assets.abstracts(1).uuid)
    }
  }

  @Test
  def testGetOwn() : Unit = {
    srv.getOwn(assets.abstracts(0).uuid, assets.alice)

    intercept[EntityNotFoundException] {
      srv.getOwn(
        assets.abstracts(0).uuid,
        Account(Some("uuid"), Some("not@valid.com"))
      )
    }

    intercept[NoResultException] {
      srv.getOwn(assets.abstracts(1).uuid, assets.eve)
    }
  }

  @Test
  def testCreate() : Unit = {
    val original = assets.createAbstract()
    val abstr = srv.create(original, assets.conferences(0), assets.alice)

    assert(abstr.uuid != null)
    assert(abstr.title == original.title)
    assert(abstr.text == original.text)

    intercept[EntityNotFoundException] {
      srv.create(original, assets.conferences(0),
                 Account(Some("uuid"), Some("foo@bar.com")))
    }

    original.uuid = "fooid"
    intercept[IllegalArgumentException] {
      srv.create(original, assets.conferences(0), assets.alice)
    }
  }

  @Test
  def testUpdate() : Unit = {
    val original = assets.abstracts(0)

    original.title = "new title"
    original.affiliations.clear()

    val abstr = srv.update(original, assets.alice)

    assert(abstr.title == original.title)
    assert(abstr.affiliations.size == 0)

    val illegal = assets.createAbstract()
    intercept[IllegalArgumentException] {
      srv.update(illegal, assets.alice)
    }

    illegal.uuid = "wringid"
    intercept[EntityNotFoundException] {
      srv.update(illegal, assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.update(original, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      srv.update(original, assets.eve)
    }
  }

  @Test
  def testDelete() : Unit = {
    val original = assets.abstracts(0)

    intercept[EntityNotFoundException] {
      srv.delete("uuid", assets.alice)
    }

    intercept[EntityNotFoundException] {
      srv.delete(original.uuid, Account(Some("uuid"), Some("foo@bar.com")))
    }

    intercept[IllegalAccessException] {
      srv.delete(original.uuid, assets.eve)
    }

    srv.delete(original.uuid, assets.alice)

    intercept[EntityNotFoundException] {
      srv.delete(original.uuid, assets.alice)
    }
  }

}


object AbstractServiceTest {

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

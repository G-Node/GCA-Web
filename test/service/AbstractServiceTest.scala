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
import javax.persistence.{EntityManagerFactory, Persistence}
import service.util.DBUtil

/**
 * Test for the abstracts service layer
 */
class AbstractServiceTest extends JUnitSuite with DBUtil {

  var emf : EntityManagerFactory = _

  var srv : AbstractService = _

  @Before
  def before() : Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    Assets.fillDB(Some(emf))
    srv = new AbstractService()
  }

  @After
  def after() : Unit = {
    Assets.killDB(Some(emf))
  }

  @Test
  def testList() : Unit = {
      intercept[NotImplementedError] {
        srv.list(null)
      }
  }

  @Test
  def testListOwn() : Unit = {
    intercept[NotImplementedError] {
      srv.listOwn(null)
    }
  }

  @Test
  def testGet() : Unit = {
    intercept[NotImplementedError] {
      srv.get(null)
    }
  }

  @Test
  def testGetOwn() : Unit = {
    intercept[NotImplementedError] {
      srv.getOwn(null, null)
    }
  }

  @Test
  def testCreate() : Unit = {
    intercept[NotImplementedError] {
      srv.create(null, null, null)
    }
  }

  @Test
  def testUpdate() : Unit = {
    intercept[NotImplementedError] {
      srv.update(null, null)
    }
  }

  @Test
  def testDelete() : Unit = {
    intercept[NotImplementedError] {
      srv.delete(null, null)
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

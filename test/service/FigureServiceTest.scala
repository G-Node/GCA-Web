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
import service.util.DBUtil


class FigureServiceTest extends JUnitSuite with DBUtil {

  var emf: EntityManagerFactory = _
  var srv: FigureService = _
  var assets: Assets = _

  @Before
  def before(): Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
    srv = new FigureService(emf, "images")
  }

  @Test
  def testGet(): Unit = {
    intercept[NotImplementedError] {
      srv.get(null)
    }
  }

  @Test
  def testCreate(): Unit = {
    intercept[NotImplementedError] {
      srv.create(null, null, null, null)
    }
  }

  @Test
  def testUpdate(): Unit = {
    intercept[NotImplementedError] {
      srv.update(null, null)
    }
  }

  @Test
  def testDelete(): Unit = {
    intercept[NotImplementedError] {
      srv.delete(null, null)
    }
  }

  @Test
  def testOpenFile(): Unit = {
    intercept[NotImplementedError] {
      srv.openFile(null)
    }
  }

}


object FigureServiceTest {

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
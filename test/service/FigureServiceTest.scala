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
import play.api.libs.Files.TemporaryFile
import java.io.File
import models.Figure


class FigureServiceTest extends JUnitSuite with DBUtil {

  var emf: EntityManagerFactory = _
  var srv: FigureService = _
  var assets: Assets = _
  var abstrsrv: AbstractService = _

  @Before
  def before(): Unit = {
    emf = Persistence.createEntityManagerFactory("defaultPersistenceUnit")
    assets = new Assets(emf)
    assets.killDB()
    assets.fillDB()
    srv = FigureService(emf, "./figures")
    abstrsrv = AbstractService(emf, assets.figPath)
  }

  @Test
  def testGet(): Unit = {
    assets.figures.foreach { figOrig =>
      val fig = srv.get(figOrig.uuid)
      assert(fig == figOrig)
    }
  }

  @Test
  def testCreate(): Unit = {
    val file = new File("tmp")
    file.createNewFile()
    val tmp = new TemporaryFile(file)
    val figOrig = Figure(None, Some("caption"))
    val fig = srv.create(figOrig, tmp, assets.abstracts(3), assets.alice)

    assert(fig.uuid != null)
    assert(fig.caption == "caption")
  }

  @Test
  def testUpdate(): Unit = {
    val fig = assets.figures(0)
    fig.caption = "foo caption"
    val figUpdated = srv.update(fig, assets.alice)
    assert(figUpdated.caption == "foo caption")
  }

  @Test
  def testDelete(): Unit = {
    val fig = assets.figures(1)
    srv.delete(fig.uuid, assets.alice)

    intercept[NoResultException] {
      srv.get(fig.uuid)
    }
  }

  @Test
  def testOpenFile(): Unit = {
    assets.figures.foreach { fig =>
      val file = srv.openFile(assets.figures(1))
      assert(file.exists())
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
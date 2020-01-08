// Copyright © 2014, German Neuroinformatics Node (G-Node)
//                   A. Stoewer (adrian.stoewer@rz.ifi.lmu.de)
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted under the terms of the BSD License. See
// LICENSE file in the root of the Project.

package service

import java.io.File

import javax.persistence._
import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.test.FakeApplication
import models.Figure
import org.apache.commons.io.FileUtils


class FigureServiceTest extends JUnitSuite {

  var srv: FigureService = _
  var assets: Assets = _
  var abstrsrv: AbstractService = _

  @Before
  def before(): Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    srv = FigureService("./figures","./figures_mobile")
    abstrsrv = AbstractService(assets.figPath)
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
    val pDir = new java.io.File(".").getCanonicalPath
    val data = new File(pDir + "/test/utils/BC_header_jpg.jpg")
    FileUtils.copyFile(data, file)
    val tmp = new TemporaryFile(file)
    val figOrig = Figure(None, Some("caption"))
    val fig = srv.create(figOrig, tmp, assets.abstracts(3), assets.alice)

    assert(fig.uuid != null)
    assert(fig.caption == "caption")
  }


  @Test
  def testUploadMobile(): Unit = {
    val formats = List("jpg", "png")
    for (format <- formats) {
      val pDir = new java.io.File(".").getCanonicalPath
      val data = new File(pDir + "/test/utils/BC_header_" + format + "." + format)

      val fileFig = new File("tmp")
      FileUtils.copyFile(data, fileFig)
      val tmpFig = new TemporaryFile(fileFig)
      val figOrig = Figure(None, Some("logo"))
      srv.create(figOrig, tmpFig, assets.abstracts(3), assets.alice)

      srv.uploadMobile(figOrig, assets.abstracts(3), assets.alice)
    }
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

  @Test
  def testOpenMobileFile(): Unit = {
    assets.figures.foreach { fig =>
      val file = srv.openMobileFile(assets.figures(1))
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
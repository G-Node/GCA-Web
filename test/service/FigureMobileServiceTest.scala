// Copyright © 2019, German Neuroinformatics Node (G-Node)
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
import models.Figure
import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.test.FakeApplication


class FigureMobileServiceTest extends JUnitSuite {

  var srv: FigureMobileService = _
  var assets: Assets = _
  var abstrsrv: AbstractService = _

  @Before
  def before(): Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    srv = FigureMobileService("./figures_mobile")
    abstrsrv = AbstractService(assets.figMobilePath)
  }

  @Test
  def testGet(): Unit = {
    assets.figures.foreach { figOrig =>
      val fig = srv.get(figOrig.uuid)
      assert(fig == figOrig)
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



object FigureMobileServiceTest {

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
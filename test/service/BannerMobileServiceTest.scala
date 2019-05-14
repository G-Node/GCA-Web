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
import models.Banner
import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.libs.Files.TemporaryFile
import play.api.test.FakeApplication


class BannerMobileServiceTest extends JUnitSuite {

  var srv: BannerMobileService = _
  var assets: Assets = _
  var confsrv: ConferenceService = _

  @Before
  def before(): Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    srv = BannerMobileService("./banner_mobile")
    confsrv = ConferenceService(assets.banMobilePath)
  }

  @Test
  def testGet(): Unit = {
    assets.banner.foreach { banOrig =>
      val ban = srv.get(banOrig.uuid)
      assert(ban == banOrig)
    }
  }

  @Test
  def testOpenFile(): Unit = {
    assets.banner.foreach { ban =>
      val file = srv.openFile(ban)
      assert(file.exists())
    }
  }

}


object BannerMobileServiceTest {

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


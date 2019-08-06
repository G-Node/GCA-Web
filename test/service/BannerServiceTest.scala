// Copyright © 2019, German Neuroinformatics Node (G-Node)
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


class BannerServiceTest extends JUnitSuite {

  var srv: BannerService = _
  var assets: Assets = _

  @Before
  def before(): Unit = {
    assets = new Assets()
    assets.killDB()
    assets.fillDB()
    srv = BannerService("./banner","./banner_mobile")
  }

  @Test
  def testGet(): Unit = {
    assets.banner.foreach { banOrig =>
      val ban = srv.get(banOrig.uuid)
      assert(ban == banOrig)
    }
  }

  @Test
  def testCreate(): Unit = {
    val fileLogo = new File("tmp")
    fileLogo.createNewFile()
    val tmpLogo = new TemporaryFile(fileLogo)
    val logoOrig = Banner(None, Some("logo"))
    val logo = srv.create(logoOrig, tmpLogo, assets.conferences(0), assets.alice)
    assert(logo.uuid != null)
    assert(logo.bType == "logo")

    val fileThn = new File("tmp")
    fileThn.createNewFile()
    val tmpThn = new TemporaryFile(fileThn)
    val thnOrig = Banner(None, Some("thumbnail"))
    val thn = srv.create(thnOrig, tmpThn, assets.conferences(0), assets.alice)
    assert(thn.uuid != null)
    assert(thn.bType == "thumbnail")
  }


  @Test
  def testDelete(): Unit = {
    val ban = assets.banner(0)
    srv.delete(ban.uuid, assets.alice)

    intercept[NoResultException] {
      srv.get(ban.uuid)
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


object BannerServiceTest {

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

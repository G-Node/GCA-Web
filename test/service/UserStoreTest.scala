package service

import org.junit.{AfterClass, Before, BeforeClass, Test}
import org.scalatest.junit.JUnitSuite
import play.api.Play
import play.api.test.FakeApplication
import models.Account


class UserStoreTest extends JUnitSuite {

  var assets: Assets = _

}



object UserStoreTest {

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
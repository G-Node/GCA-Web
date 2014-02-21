package test

import org.junit._
import org.scalatest.junit.JUnitSuite
import play.api.test.FakeApplication
import play.api.Play

import models._
import utils.serializer._

/**
 * Tests for Serializer
 */
class SerializerTest extends JUnitSuite {

  val baseUrl = "http://hostname:8000"

  @Test
  def testConference(): Unit = {
    val jsFormat = new ConferenceFormat(baseUrl)

    val original = Conference(Option("foo"), Option("bar"))
    val json = jsFormat.writes(original)

    jsFormat.reads(json).fold(
      valid = { converted => {assert(converted.name == original.name)}},
      invalid = { errors => throw new MatchError(errors.toString()) }
    )
  }
}

object SerializerTest {

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
